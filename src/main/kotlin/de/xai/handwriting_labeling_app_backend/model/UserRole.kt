package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority

@Entity
@Table(name = "role")
data class UserRole (
    @Column(name = "id")
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false)
    val name: String? = null
): GrantedAuthority {
    override fun getAuthority(): String {
        return this.name!!
    }
}