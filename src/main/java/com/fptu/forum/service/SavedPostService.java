package com.fptu.forum.service;

import com.fptu.forum.entity.Post;
import com.fptu.forum.entity.SavedPost;
import com.fptu.forum.entity.User;

import java.util.List;

/**
 * Interface cho SavedPostService.
 * Implementation: SavedPostServiceImpl.
 */
public interface SavedPostService {

    /**
     * Toggle bookmark bai viet.
     * @return true neu vua bookmark, false neu vua unbookmark.
     */
    boolean toggleSave(User user, Post post);

    List<SavedPost> listSavedPosts(Long userId);

    boolean isSaved(Long userId, Long postId);
}
