package Bot.Repository;

import Bot.Domain.HeroesRatingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface HeroesRatingRepository extends JpaRepository<HeroesRatingEntity, Long> {

    public HeroesRatingEntity getByHeroNameAndSenderPSID(String heroName, Long senderPSID);

    @Query(value = "select heroes_rating.hero_name from heroes_rating where rating = 1  group by (hero_name)" +
            " order by count(heroes_rating.rating) desc limit 10", nativeQuery = true)
    public Set<String> getTopHeroesForQuickReply();
}
