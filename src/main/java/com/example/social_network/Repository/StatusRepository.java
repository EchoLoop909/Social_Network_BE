package com.example.social_network.Repository;

import com.example.social_network.Payload.Util.Status;
import com.example.social_network.models.Entity.Statususer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<Statususer,String> {
}
