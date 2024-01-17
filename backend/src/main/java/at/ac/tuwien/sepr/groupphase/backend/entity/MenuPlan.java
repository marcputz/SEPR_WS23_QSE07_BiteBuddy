package at.ac.tuwien.sepr.groupphase.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class MenuPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ApplicationUser user;

    // TODO: add not null constraint back once profiles are implemented
    // @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Profile profile;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate fromDate;

    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    private LocalDate untilDate;

    @OneToMany(mappedBy = "menuplan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MenuPlanContent> content = new HashSet<>();

    /* CONTENT MANAGEMENT METHODS */

    public void addContent(MenuPlanContent c) {
        content.add(c);
        c.setMenuplan(this);
    }

    public void removeContent(MenuPlanContent c) {
        content.remove(c);
        c.setMenuplan(null);
    }

    /* GETTERS and SETTERS */

    public Long getId() {
        return id;
    }

    public MenuPlan setId(Long id) {
        this.id = id;
        return this;
    }

    public ApplicationUser getUser() {
        return user;
    }

    public MenuPlan setUser(ApplicationUser user) {
        this.user = user;
        return this;
    }

    public Profile getProfile() {
        return profile;
    }

    public MenuPlan setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public MenuPlan setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
        return this;
    }

    public LocalDate getUntilDate() {
        return untilDate;
    }

    public MenuPlan setUntilDate(LocalDate untilDate) {
        this.untilDate = untilDate;
        return this;
    }

    public Set<MenuPlanContent> getContent() {
        return content;
    }

    public MenuPlan setContent(Set<MenuPlanContent> content) {
        this.content = content;
        return this;
    }

    /* EQUALS and HASHCODE */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MenuPlan menuPlan = (MenuPlan) o;
        return Objects.equals(id, menuPlan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /* TO_STRING */

    @Override
    public String toString() {
        return "MenuPlan{"
            + "id=" + id
            + ", user=" + user
            + ", profile=" + profile
            + ", fromDate=" + fromDate
            + ", untilDate=" + untilDate
            + ", content=" + content
            + '}';
    }
}
