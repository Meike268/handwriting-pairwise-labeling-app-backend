package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


@Entity
@Table(name = "user")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    private val username: String? = null,

    @Column(nullable = false)
    private val password: String? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")]
    )
    var roles: Set<Role> = mutableSetOf()
): UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.roles.toMutableSet()
    }

    override fun getPassword(): String {
        return this.password!!
    }

    override fun getUsername(): String {
        return this.username!!
    }
}
