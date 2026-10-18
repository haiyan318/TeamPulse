package com.example.teampulse.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.teampulse.entity.GroupMember;
import com.example.teampulse.mapper.GroupMemberMapper;
import com.example.teampulse.service.GroupMemberService;
import org.springframework.stereotype.Service;

/**
 * 小组成员表 Service 实现。
 */
@Service
public class GroupMemberServiceImpl extends ServiceImpl<GroupMemberMapper, GroupMember>
        implements GroupMemberService {
}
