package vn.hutech.trandinhty_2280618597.entities;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Field("username")
    private String username;

    @Field("email")
    private String email;

    @Field("password")
    private String password;

    @DBRef
    private Set<Role> roles = new HashSet<>();

    @Field("provider")
    private String provider; // LOCAL, GOOGLE

    @Field("provider_id")
    private String providerId; // Google sub ID

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.provider = "LOCAL";
    }
}
