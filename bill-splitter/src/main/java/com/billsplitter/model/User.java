package com.billsplitter.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false) private String username;
    @Column(nullable = false) private String password;
    @Column(nullable = false) private String displayName;
    @Column(unique = true, nullable = false) private String email;
    @Enumerated(EnumType.STRING) private Role role;
    @OneToMany(mappedBy = "spentBy", cascade = CascadeType.ALL)
    private List<Expense> expensesPosted;
    public enum Role { USER, ADMIN }
}
