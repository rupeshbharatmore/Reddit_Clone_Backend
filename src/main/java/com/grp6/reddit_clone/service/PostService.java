package com.grp6.reddit_clone.service;

import java.util.List;

import static java.util.stream.Collectors.toList;

import com.grp6.reddit_clone.dto.PostRequest;
import com.grp6.reddit_clone.dto.PostResponse;
import com.grp6.reddit_clone.exceptions.PostNotFoundException;
import com.grp6.reddit_clone.exceptions.SubredditNotFoundException;
import com.grp6.reddit_clone.mapper.PostMapper;
import com.grp6.reddit_clone.model.Post;
import com.grp6.reddit_clone.model.Subreddit;
import com.grp6.reddit_clone.model.User;
import com.grp6.reddit_clone.repository.PostRepository;
import com.grp6.reddit_clone.repository.SubredditRepository;
import com.grp6.reddit_clone.repository.UserRepository;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final SubredditRepository subredditRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final PostMapper postMapper;

    public void save(PostRequest postRequest) {
        Subreddit subreddit = subredditRepository.findByName(postRequest.getSubredditName())
                .orElseThrow(() -> new SubredditNotFoundException(postRequest.getSubredditName()));
        Post post = postMapper.map(postRequest, subreddit, authService.getCurrentUser());
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id.toString()));
        return postMapper.mapToDto(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream().map(postMapper::mapToDto).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsBySubreddit(Long subredditId) {
        Subreddit subreddit = subredditRepository.findById(subredditId)
                .orElseThrow(() -> new SubredditNotFoundException(subredditId.toString()));
        List<Post> posts = postRepository.findAllBySubreddit(subreddit);
        return posts.stream().map(postMapper::mapToDto).collect(toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return postRepository.findByUser(user).stream().map(postMapper::mapToDto).collect(toList());
    }
}
