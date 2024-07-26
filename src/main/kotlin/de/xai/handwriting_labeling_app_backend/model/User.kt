package de.xai.handwriting_labeling_app_backend.model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


@Entity
@Table(name = "user")
class User(
    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "username", nullable = false, unique = true)
    private val username: String? = null,

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false)
    private val password: String? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_role",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: Set<Role> = mutableSetOf()
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.roles.toMutableSet()
    }

    override fun getPassword(): String {
        return this.password!!
    }

    override fun getUsername(): String {
        return this.username!!
    }

    override fun toString(): String {
        return "User(id=$id, username=$username, password=$password, roles=$roles)"
    }
}
