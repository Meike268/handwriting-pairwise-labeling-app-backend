package de.xai.handwriting_labeling_app_backend.model

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "user_role")
@IdClass(UserRoleId::class)
class UserRole(

    @JoinColumn(name = "user_id")
    @Id
    @ManyToOne
    var user: User? = null,

    @JoinColumn(name = "role_id")
    @Id
    @ManyToOne
    var role: Role? = null
)

class UserRoleId(
    private val user: User,
    private val role: Role
) : Serializable