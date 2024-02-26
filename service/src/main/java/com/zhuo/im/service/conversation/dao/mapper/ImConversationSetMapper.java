package com.zhuo.im.service.conversation.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhuo.im.service.conversation.dao.ImConversationSetEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * @description:
 * @version: 1.0
 */
@Mapper
public interface ImConversationSetMapper extends BaseMapper<ImConversationSetEntity> {

    @Update(" update im_conversation_set set read_sequence = #{readSequence}, sequence = #{sequence} " +
    " where conversation_id = #{conversationId} and app_id = #{appId} AND read_sequence < #{readSequence}")
    public void markRead(ImConversationSetEntity imConversationSetEntity);

}
