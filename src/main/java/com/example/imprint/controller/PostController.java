package com.example.imprint.controller;

import com.example.imprint.domain.BoardEntity;
import com.example.imprint.domain.user.UserEntity;
import com.example.imprint.domain.post.PostRequestDto;
import com.example.imprint.domain.post.PostResponseDto;
import com.example.imprint.repository.BoardRepository;
import com.example.imprint.service.PostService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final BoardRepository boardRepository;

    @GetMapping("/list")
    public String list(@RequestParam("board") BoardEntity board,
                       @PageableDefault(size = 10) Pageable pageable,
                       Model model) {
        Page<PostResponseDto> postList = postService.getPostList(board, pageable);
        model.addAttribute("posts", postList);
        return "board/list";
    }

    @GetMapping("/read")
    public String read(@RequestParam("id") Long id,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        PostResponseDto post = postService.getPost(id);
        model.addAttribute("post", post);
        model.addAttribute("page", page);
        return "board/read";
    }

    @GetMapping("/write")
    public String writeForm(HttpSession session, Model model) {
        if (session.getAttribute("loginUser") == null) {
            return "redirect:/account/login";
        }
        model.addAttribute("postDto", new PostRequestDto());
        return "board/write";
    }

    @PostMapping("/write")
    public String write(@Valid @ModelAttribute("postDto") PostRequestDto dto,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        UserEntity user = (UserEntity) session.getAttribute("loginUser");
        if (user == null) return "redirect:/account/login";

        if (bindingResult.hasErrors()) return "board/write";

        Long postId = postService.writePost(dto, user);

        redirectAttributes.addAttribute("id", postId);
        return "redirect:/board/read";
    }

    @PostMapping("/update")
    public String update(@RequestParam("id") Long id,
                         @Valid @ModelAttribute("postDto") PostRequestDto dto,
                         BindingResult bindingResult,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        UserEntity user = (UserEntity) session.getAttribute("loginUser");
        if (bindingResult.hasErrors()) return "board/update";

        postService.updatePost(id, dto, user);
        redirectAttributes.addAttribute("id", id);
        return "redirect:/board/read";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("loginUser");
        postService.deletePost(id, user);
        return "redirect:/board/list";
    }
}
