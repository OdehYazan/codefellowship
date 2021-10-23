package com.example.codefellowship.controllers;

import com.example.codefellowship.domain.ApplicationUser;
import com.example.codefellowship.domain.Post;
import com.example.codefellowship.repository.ApplicationUserRepository;
import com.example.codefellowship.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ApplicationController {

    @Autowired
    PostRepository postRepository;
    @Autowired
    ApplicationUserRepository applicationUserRepository;

    @Autowired
    BCryptPasswordEncoder encoder;


    @GetMapping("/signup")
    public String getSignUpPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public RedirectView attemptSignUp(@RequestParam String username, @RequestParam String password,
                                      @RequestParam String firstName, @RequestParam String lastName, @RequestParam String dateOfBirth, @RequestParam String bio) {
        ApplicationUser newUser = new ApplicationUser(username, encoder.encode(password), firstName, lastName, dateOfBirth, bio);
        newUser=applicationUserRepository.save(newUser);


        Authentication authentication = new UsernamePasswordAuthenticationToken(newUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return new RedirectView("/");
    }
    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/profile")
    public String getProfilePage(Model model) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        model.addAttribute("username", userDetails.getUsername());
        List<Post> posts = postRepository.findAllByUser(userDetails);
        model.addAttribute("postList",posts);
        model.addAttribute("user", applicationUserRepository.findApplicationUserByUsername(userDetails.getUsername()));
        return "profile";
    }
    @PostMapping("/addpost")
    public RedirectView addPost(@AuthenticationPrincipal ApplicationUser user, @RequestParam String body) {
        ApplicationUser newUser = applicationUserRepository.findApplicationUserByUsername(user.getUsername());
        Post addNewPost = new Post(body, newUser);
        postRepository.save(addNewPost);
        return new RedirectView("/profile");
    }

    @GetMapping("/feed")
    public String getFeed(Principal p, Model m) {
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(p.getName());
        m.addAttribute("applicationUser", applicationUser);
        return "feed";
    }
    @GetMapping("/users")
    public String getAllUsers(Principal p, Model m) {
        ApplicationUser applicationUser = applicationUserRepository.findApplicationUserByUsername(p.getName());
        List<ApplicationUser> allUsers = applicationUserRepository.findAll();
        m.addAttribute("applicationUser", applicationUser);
        m.addAttribute("allUsers", allUsers);
        return "users";
    }
    @PostMapping("/users/follow")
    public RedirectView addFollower(long followedUser, Principal p) {
        ApplicationUser primaryUser = applicationUserRepository.findApplicationUserByUsername(p.getName());
        primaryUser.addFollower(applicationUserRepository.findById(followedUser).get());
        applicationUserRepository.save(primaryUser);
        return new RedirectView("/users");
    }
    @PostMapping("/users/unfollow")
    public RedirectView removeFollower(long unfollowedUser, Principal p) {
        ApplicationUser primaryUser = applicationUserRepository.findApplicationUserByUsername(p.getName());
        primaryUser.removeFollower(applicationUserRepository.findById(unfollowedUser).get());
        applicationUserRepository.save(primaryUser);
        return new RedirectView("/users");
    }
}
