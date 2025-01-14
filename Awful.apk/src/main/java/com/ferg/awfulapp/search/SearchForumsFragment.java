/********************************************************************************
 * Copyright (c) 2012, Matthew Shepard
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the software nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SCOTT FERGUSON ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCOTT FERGUSON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/

package com.ferg.awfulapp.search;

import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.ferg.awfulapp.AwfulDialogFragment;
import com.ferg.awfulapp.R;
import com.ferg.awfulapp.network.NetworkUtils;
import com.ferg.awfulapp.task.AwfulRequest;
import com.ferg.awfulapp.task.SearchForumsFilterRequest;
import com.ferg.awfulapp.thread.AwfulSearchForum;

import java.util.ArrayList;

public class SearchForumsFragment extends AwfulDialogFragment {
	private final static String TAG = "SearchForumsFragment";

    private SearchFragment parent;
	private RecyclerView mSearchForums;
	private ArrayList<AwfulSearchForum> forums;
	private ProgressBar mProgress;

    public SearchForumsFragment(SearchFragment parent) {
        super();
        this.parent = parent;
    }

    public SearchForumsFragment() {
        super();
    }

    @Override
	public void onActivityCreated(Bundle aSavedState) {
		super.onActivityCreated(aSavedState);
		getDialog().setTitle(getTitle());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View result = inflateView(R.layout.search_forums_dialog, container, inflater);
		mProgress = result.findViewById(R.id.search_forums_progress);
		mSearchForums = result.findViewById(R.id.select_forums);
		mSearchForums.setAdapter(new RecyclerView.Adapter<SearchHolder>() {
			@Override
			public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
				View view = LayoutInflater.from(parent.getContext())
						.inflate(R.layout.search_forum_item, parent, false);

				getAwfulActivity().setPreferredFont(view);
				return new SearchHolder(view);
			}

			@Override
			public void onBindViewHolder(SearchHolder holder, final int position) {
				final AwfulSearchForum searchForum = forums.get(position);
				holder.forumName.setText(searchForum.getForumName());
				holder.forumCheckbox.setChecked(searchForum.isChecked());
				holder.forumCheckbox.setTag(searchForum);
				final RecyclerView.Adapter<SearchHolder> self = this;
				holder.forumCheckbox.setOnClickListener(v -> {
                    CheckBox cb = (CheckBox) v;
                    AwfulSearchForum forum = (AwfulSearchForum) cb.getTag();

                    forum.setChecked(cb.isChecked());
                    searchForum.setChecked(cb.isChecked());
                    if(cb.isChecked()){
                        parent.getSearchForums().add(forum.getForumId());
                    }else{
                        parent.getSearchForums().remove(forum.getForumId());
                    }
                    if(searchForum.getDepth() <3 ){
                        for (AwfulSearchForum childSearchForum: forums){
                            if(childSearchForum.getDepth()>searchForum.getDepth() || searchForum.getForumId() == -1){
                                if(childSearchForum.getParents().contains("parent"+searchForum.getForumId()) || searchForum.getForumId() == -1){
                                    childSearchForum.setChecked(searchForum.isChecked());
                                    if(searchForum.isChecked()){
                                        parent.getSearchForums().add(childSearchForum.getForumId());
                                    }else{
                                        parent.getSearchForums().remove(childSearchForum.getForumId());
                                    }
                                }
                            }
                        }
                        self.notifyDataSetChanged();
                    }
                });
				getAwfulActivity().setPreferredFont(holder.itemView);
			}

			@Override
			public int getItemCount() {
				if (forums != null) {
					return forums.size();
				}
				return 0;
			}
		});
		mSearchForums.setLayoutManager(new LinearLayoutManager(getContext()));

		getForums();

		return result;
	}


	@Override
	public String getTitle() {
		return getString(R.string.search_forums_select_forums);
	}

	@Override
	public boolean volumeScroll(KeyEvent event) {
		return false;
	}

	public void getForums(){
		NetworkUtils.queueRequest(new SearchForumsFilterRequest(this.getContext()).build(null, new AwfulRequest.AwfulResultCallback<ArrayList<AwfulSearchForum>>() {

			@Override
			public void success(ArrayList<AwfulSearchForum> result) {
				forums = result;
				for(AwfulSearchForum searchForum: forums){
						searchForum.setChecked(parent.getSearchForums().contains(searchForum.getForumId()));
				}
				mProgress.setVisibility(View.GONE);
				mSearchForums.setVisibility(View.VISIBLE);
				mSearchForums.getAdapter().notifyDataSetChanged();
			}

			@Override
			public void failure(VolleyError error) {
				Snackbar.make(getView() != null ? getView() : parent.getView(), R.string.search_forums_failure_message, Snackbar.LENGTH_LONG)
						.setAction("Retry", v -> getForums()).show();
			}
		}));
	}


	private class SearchHolder extends RecyclerView.ViewHolder {
		final TextView forumName;
		final CheckBox forumCheckbox;
		SearchHolder(View view) {
			super(view);
			forumName = itemView.findViewById(R.id.search_forum_name);
			forumCheckbox = itemView.findViewById(R.id.search_forum_checkbox);
		}
	}
}
