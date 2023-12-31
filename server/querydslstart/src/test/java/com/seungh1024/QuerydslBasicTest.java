package com.seungh1024;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.seungh1024.dto.MemberDto;
import com.seungh1024.dto.QMemberDto;
import com.seungh1024.dto.UserDto;
import com.seungh1024.entity.Member;
import com.seungh1024.entity.QMember;
import com.seungh1024.entity.QTeam;
import com.seungh1024.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.TypedQuery;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
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

    @Test
    public void oneProjection(){
        List<String> members = queryFactory
                .select(QMember.member.username)
                .from(QMember.member)
                .fetch();

        for (String s : members){
            System.out.println("s = "+s);
        }
    }

    @Test
    public void tupleProjection(){
        List<Tuple> members = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : members) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username + ", age = "+ age);
        }
    }

    @Test
    public void findDtoByJPQL(){
        String jpqlString = "select new com.seungh1024.dto.MemberDto(m.username, m.age) from Member m ";
        List<MemberDto> result = em.createQuery(jpqlString, MemberDto.class).getResultList();

        for(MemberDto memberDto :result){
            System.out.println("memberDto = " + memberDto);
        }

    }

    @Test
    public void findDtoBySetter(){
        List<MemberDto> members = queryFactory
                .select(Projections.bean(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : members){
            System.out.println(memberDto);
        }
    }

    @Test
    public void findDtoByField(){
        List<MemberDto> members = queryFactory
                .select(Projections.fields(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : members){
            System.out.println(memberDto);
        }
    }

    @Test
    public void findDtoByConstructor(){
        List<MemberDto> members = queryFactory
                .select(Projections.constructor(MemberDto.class, member.username, member.age))
                .from(member)
                .fetch();

        for(MemberDto memberDto : members){
            System.out.println(memberDto);
        }
    }

    @Test
    public void findUserDto(){
        QMember subMember = new QMember("subMember");
        List<UserDto> members = queryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        ExpressionUtils.as(
                                JPAExpressions
                                        .select(subMember.age.max())
                                        .from(subMember),"age")
                        ))
                .from(member)
                .fetch();

        for(UserDto userDto : members){
            System.out.println(userDto);
        }
    }

    @Test
    public void findDtoByQueryProjection(){
        List<MemberDto> members = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : members){
            System.out.println(memberDto);
        }
    }

    @Test
    public void booleanBuilder(){
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> members = searchMember1(usernameParam,ageParam);
        Assertions.assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();
        if(usernameParam != null){
            builder.and(member.username.eq(usernameParam));
        }

        if(ageParam != null){
            builder.and(member.age.eq(ageParam));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void whereParam(){
        String usernameParam = "member1";
        Integer ageParam = null;

        List<Member> members = searchMember2(usernameParam,ageParam);
        Assertions.assertThat(members.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
//                .where(usernameEq(usernameParam), ageEq(ageParam))
                .where(allEq(usernameParam,ageParam))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameParam) {
        if(usernameParam == null){
            return null;
        }else{
            return member.username.eq(usernameParam);
        }
    }

    private BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) :null;
    }

    private BooleanExpression allEq(String usernameParam, Integer ageParam){
        return usernameEq(usernameParam).and(ageEq(ageParam));
    }

    @Test
    @Commit
    public void bulkUpdate(){
        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(20))
                .execute();

        em.flush();
        em.clear();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        for(Member member1 : result){
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void bulkAdd(){
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }

    @Test
    @Commit
    public void bulkDelete(){
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();
        for(Member member1 : result){
            System.out.println("member1 = " + member1);
        }
    }

    @Test
    public void sqlFunction(){
        List<String> result = queryFactory
                .select(
                        Expressions.stringTemplate(
                                "function('replace',{0},{1},{2})",
                                member.username,
                                "member",
                                "M"
                        )
                )
                .from(member)
                .fetch();

        for (String s :result){
            System.out.println("s = " + s);
        }
    }

    @Test
    public void sqlFunction2(){
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
//                .where(member.username.eq(
//                        Expressions.stringTemplate(
//                                "function('lower',{0})",
//                                member.username
//                        )
//                ))
                .where(member.username.eq(member.username.lower()))
                .fetch();

        for (String s :result){
            System.out.println("s = "+s);
        }
    }
}
