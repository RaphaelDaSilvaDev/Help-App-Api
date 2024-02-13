package com.raphaelsilva.help.app.repository

import com.raphaelsilva.help.app.model.Roles
import org.springframework.data.jpa.repository.JpaRepository

interface RoleRepository: JpaRepository<Roles, Long>  {

}