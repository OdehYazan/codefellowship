package com.example.codefellowship.repository;

import com.example.codefellowship.domain.ApplicationUser;
import com.example.codefellowship.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post,Long> {
    List<Post> findAllByUser(UserDetails user);
}
