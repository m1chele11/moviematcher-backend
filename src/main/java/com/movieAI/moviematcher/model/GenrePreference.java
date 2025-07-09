package com.movieAI.moviematcher.model;

import jakarta.persistence.*;




@Entity
@Table(name = "genre_preferences")
public class GenrePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private String genreName;
//    private int ranking;
//    private int userSlot;
//    private Integer userId;

    @Column(name = "genre_name", nullable = false)
    private String genreName;

    @Column(nullable = false)
    private int ranking;

    // To distinguish between user 1 and user 2 preferences
    @Column(name = "user_slot", nullable = false)
    private int userSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getUserSlot() {
        return userSlot;
    }

    public void setUserSlot(int userSlot) {
        this.userSlot = userSlot;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }
}
