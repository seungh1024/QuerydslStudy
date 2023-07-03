package com.seungh1024.entity;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 테스트에서 트랜잭션은 기본적으로 롤백되게 설정되어 있다.
//@Commit // 그래서 커밋 어노테이션을 달아주면 롤백되지 않고 DB에 반영된다.
// 다음 테스트에 이거 때문에 데이터 남아서 테스트 이상해질 수 있음. 그래서 테스트 하나만 해서 DB 확인하고 싶을 때 사용
class MemberTest {
    @Autowired
    EntityManager em;

    @Test
    public void testEntity(){
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

        // 영속성 컨텍스트 초기화
        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for(Member member : members){
            System.out.println("member = " +member);
            System.out.println("-> member.team" + member.getTeam());
        }
    }
}