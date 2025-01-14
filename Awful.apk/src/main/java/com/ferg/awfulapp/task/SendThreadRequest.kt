package com.ferg.awfulapp.task

import android.content.ContentValues
import android.content.Context
import com.ferg.awfulapp.constants.Constants.*
import com.ferg.awfulapp.network.NetworkUtils
import com.ferg.awfulapp.thread.AwfulMessage
import com.ferg.awfulapp.thread.AwfulPost
import org.jsoup.nodes.Document

/**
 * Submit a thread described by a set of ContentValues
 */
class SendThreadRequest(context: Context, reply: ContentValues)
    : AwfulRequest<Void?>(context, FUNCTION_POST_THREAD, isPostRequest = true) {

    init {
        with(parameters) {
            add(PARAM_ACTION, "postthread")
            add(PARAM_FORUM_ID, Integer.toString(reply.getAsInteger(AwfulMessage.ID)!!))
            add(PARAM_FORMKEY, reply.getAsString(AwfulPost.FORM_KEY))
            add(PARAM_FORM_COOKIE, reply.getAsString(AwfulPost.FORM_COOKIE))
            add(PARAM_SUBJECT, NetworkUtils.encodeHtml(reply.getAsString(AwfulMessage.POST_SUBJECT)))
            add(PARAM_MESSAGE, NetworkUtils.encodeHtml(reply.getAsString(AwfulMessage.POST_CONTENT)))
            add(PARAM_PARSEURL, YES)
            add("iconid", reply.getAsString(AwfulMessage.POST_ICON_ID))
            if (reply.getAsString(AwfulPost.FORM_BOOKMARK).equals("checked", ignoreCase = true)) {
                add(PARAM_BOOKMARK, YES)
            }
            listOf(AwfulMessage.REPLY_SIGNATURE, AwfulMessage.REPLY_DISABLE_SMILIES)
                    .forEach { key -> if (reply.containsKey(key)) add(key, YES) }
            reply.getAsString(AwfulMessage.REPLY_ATTACHMENT)?.let { filePath ->
                attachFile(PARAM_ATTACHMENT, filePath)
            }
        }
    }

    override fun handleResponse(doc: Document): Void? = null

}
