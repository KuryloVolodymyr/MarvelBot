package Bot.Domain;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Table(name = "heroesRating")
@EntityListeners(AuditingEntityListener.class)
public class HeroesRatingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String heroName;

    @Column
    private Long senderPSID;

    @Column
    private Boolean rating;

    public HeroesRatingEntity(String heroName, Long senderPSID, String ratingSymbol){
        this.heroName = heroName;
        this.senderPSID = senderPSID;
        if(ratingSymbol.equals("\uD83D\uDC4D")){
            this.rating = true;
        }
        else {
            this.rating = false;
        }
    }

    public HeroesRatingEntity(){

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHeroName() {
        return heroName;
    }

    public void setHeroName(String heroName) {
        this.heroName = heroName;
    }

    public Long getSenderPSID() {
        return senderPSID;
    }

    public void setSenderPSID(Long senderPSID) {
        this.senderPSID = senderPSID;
    }

    public Boolean getRating() {
        return rating;
    }

    public void setRating(Boolean rating) {
        this.rating = rating;
    }
}