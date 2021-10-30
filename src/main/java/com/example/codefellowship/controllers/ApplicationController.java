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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApplicationUser newUser = applicationUserRepository.findApplicationUserByUsername(user.getUsername());
        Post addNewPost = new Post(body, newUser);
        postRepository.save(addNewPost);
        return new RedirectView("/profile");
    }
    @GetMapping("/user/{id}")
    public String getOneUser(@PathVariable long id, Principal p, Model m) {
        ApplicationUser otherUser = applicationUserRepository.findById(id).get();
       ApplicationUser currentUser = applicationUserRepository.findApplicationUserByUsername(p.getName());
        m.addAttribute("otherUser", otherUser);
        m.addAttribute("currentUser", currentUser);
        return "oneUser";
    }

    @GetMapping("/allUsers")
    public String getAllUsers(Principal principal,Model model){
        try{
            model.addAttribute("userData",principal.getName());
            model.addAttribute("AllUsers",applicationUserRepository.findAll());

            ApplicationUser user = applicationUserRepository.findApplicationUserByUsername(principal.getName());
            model.addAttribute("userFollow",user.getFollowers());
        }catch (NullPointerException e){
            model.addAttribute("userData","");
        }
        return "allUsers";
    }
    @Transactional
    @PostMapping("/follow")
    public RedirectView addFollow(Principal principal, @RequestParam Long id){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ApplicationUser user = applicationUserRepository.findApplicationUserByUsername(userDetails.getUsername());
        System.out.println("================================================================================");
        System.out.println(user);
        System.out.println("================================================================================");
        ApplicationUser toFollow = applicationUserRepository.findById(id).get();
        System.out.println("================================================================================");
        System.out.println(toFollow);
        System.out.println("================================================================================");
        user.getFollowers().add(toFollow);

        applicationUserRepository.save(user);
        return new RedirectView("/feed");
    }
    @GetMapping("/feed")
    public String getFollowingInfo(Principal principal, Model model){
        try{
            model.addAttribute("userData",principal.getName());
            ApplicationUser user = applicationUserRepository.findApplicationUserByUsername(principal.getName());

            Set<ApplicationUser> userFollow = user.getFollowers();


            model.addAttribute("Allfollowing",userFollow);
        }catch (NullPointerException e){
            model.addAttribute("userData","");
        }
        return "feed";
    }


//    @Transactional
//    @GetMapping("/getPosts")
//    public RedirectView getposts(Principal principal, @RequestParam Long id,Model model){
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        ApplicationUser user = applicationUserRepository.findById(id).get();
//         List<Post> posts=user.getPosts();
//        model.addAttribute("posts",posts);
//
//
//        return new RedirectView("/feed");
//    }


}
