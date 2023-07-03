package com.seungh1024;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seungh1024.entity.Member;
import com.seungh1024.entity.QMember;
import com.seungh1024.entity.QTeam;
import com.seungh1024.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.seungh1024.entity.QMember.*;
import static com.seungh1024.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest{
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);

        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        Member memberNull= new Member(null,100);
        Member member5 = new Member("member5",100);
        Member member6 = new Member("member6",100);

        em.persist(memberNull);
        em.persist(member5);
        em.persist(member6);

    }

    @Test
    public void startJPQL(){
        String jpqlString = "select m from Member m where m.username = :username";
        Member findMemberByJpQL = em.createQuery(jpqlString,Member.class)
                .setParameter("username","member1")
                .getSingleResult();

        Assertions.assertThat(findMemberByJpQL.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
//        QMember qMember = new QMember("m"); // Q클래스 생성할 때 속성값을 주는데 어떤 QMember인지 구분하기 위한 값임
//        QMember qMember = member;

        Member findMemberByQ = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        Assertions.assertThat(findMemberByQ.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        .and(member.age.eq(10)))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndWithParam(){
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1")
                        ,member.age.eq(10))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch(){
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchOneLimit = queryFactory
                .selectFrom(member)
                .fetchFirst();

    }

    @Test
    public void sort(){
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = members.get(0);
        Member member6 = members.get(1);
        Member memberNull = members.get(2);
        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1(){
        List<Member> members = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        Assertions.assertThat(members.size()).isEqualTo(2);
    }

    @Test
    public void aggregation(){
        em.persist(new Member(null,0));
        em.flush();
        em.clear();

        List<Tuple> members = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = members.get(0);
        Assertions.assertThat(tuple.get(member.count())).isEqualTo(8);
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(400);
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(50);
        Assertions.assertThat(tuple.get(member.age.max())).isEqualTo(100);
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(00);
    }

    @Test
    public void groupBy() throws Exception{
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertThat(teamA.get(team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        Assertions.assertThat(teamB.get(team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    public void join() throws Exception{
        List<Member> members = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        Member member = members.get(0);
        Assertions.assertThat(member.getTeam().getName()).isEqualTo("teamA");

        Assertions.assertThat(members)
                .extracting("username")
                .containsExactly("member1","member2");
    }

    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> members = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        Assertions.assertThat(members)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }

    @Test
    public void join_on_filtering(){
        List<Tuple> members = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for(Tuple tuple : members){
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void join_on_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = "+tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void normalJoin(){
        em.flush();
        em.clear();

        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam()); // 초기화 됐는지 안됐는지 알려준다
        Assertions.assertThat(loaded).as("페치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoin(){
        em.flush();
        em.clear();

        Member member1 = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam()); // 초기화 됐는지 안됐는지 알려준다
        Assertions.assertThat(loaded).as("페치 조인 미적용").isTrue();
    }

    @Test
    public void subQuery(){
        QMember subMember = new QMember("subMember");
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(subMember.age.max())
                                .from(subMember)
                ))
                .fetch();

        Member member = members.get(0);
        Assertions.assertThat(member.getAge()).isEqualTo(100);
    }

    @Test
    public void subQueryGoe(){
        QMember subMember = new QMember("subMember");
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember)
                ))
                .fetch();

        Assertions.assertThat(members).extracting("age")
                .containsExactly(100,100,100);
    }

    @Test
    public void subQueryIn(){
        QMember subMember = new QMember("subMember");
        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.gt(50))
                ))
                .fetch();


        Assertions.assertThat(members).extracting("age")
                .containsExactly(100,100,100);
    }

    @Test
    public void subQuerySelect(){
        QMember subMember = new QMember("subMember");
        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(subMember.age.avg())
                                .from(subMember)
                )
                .from(member)
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple = "+tuple);
        }

    }

    @Test
    public void basicCase(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for(String s : result){
            System.out.println("s = "+s);
        }
    }

    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for(String s : result){
            System.out.println("s = "+s);
        }
    }

    @Test
    public void constant(){
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for(Tuple tuple : result){
            System.out.println("tuple = "+tuple);
        }
    }

    @Test
    public void concat(){
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .fetch();

        for(String s : result){
            System.out.println("s = "+s);
        }
    }
}
