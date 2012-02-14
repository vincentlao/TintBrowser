package org.tint.ui.fragments;

import java.util.List;

import org.tint.R;
import org.tint.addons.AddonMenuItem;
import org.tint.controllers.Controller;
import org.tint.model.BookmarkHistoryItem;
import org.tint.model.BookmarksAdapter;
import org.tint.providers.BookmarksProvider;
import org.tint.providers.BookmarksWrapper;
import org.tint.ui.UIManager;
import org.tint.ui.activities.EditBookmarkActivity;
import org.tint.utils.ApplicationUtils;
import org.tint.utils.Constants;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class BookmarksFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private static final int CONTEXT_MENU_OPEN_IN_TAB = Menu.FIRST;
	private static final int CONTEXT_MENU_EDIT_BOOKMARK = Menu.FIRST + 1;
	private static final int CONTEXT_MENU_COPY_URL = Menu.FIRST + 2;
	private static final int CONTEXT_MENU_SHARE_URL = Menu.FIRST + 3;
	private static final int CONTEXT_MENU_DELETE_BOOKMARK = Menu.FIRST + 4;
	
	private View mContainer = null;
	
	private UIManager mUIManager;
	
	private GridView mBookmarksGrid;
	
	private BookmarksAdapter mAdapter;
	
	public BookmarksFragment() {
		mUIManager = Controller.getInstance().getUIManager();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		String[] from = new String[] { BookmarksProvider.Columns.TITLE, BookmarksProvider.Columns.URL };
		int[] to = new int[] { R.id.BookmarkRow_Title, R.id.BookmarkRow_Url };
		
		mAdapter = new BookmarksAdapter(
				getActivity(),
				R.layout.bookmark_row,
				null,
				from,
				to,
				ApplicationUtils.getBookmarksThumbnailsDimensions(getActivity()),
				R.drawable.browser_thumbnail);
		
		mBookmarksGrid.setAdapter(mAdapter);
		
		mBookmarksGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				BookmarkHistoryItem item = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), id);
				
				if (item != null) {
					Intent result = new Intent();
					result.putExtra(Constants.EXTRA_URL, item.getUrl());
					
					getActivity().setResult(Activity.RESULT_OK, result);
					getActivity().finish();
				}
			}
		});
		
		registerForContextMenu(mBookmarksGrid);
		
		getLoaderManager().initLoader(0, null, this);
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mContainer == null) {
			mContainer = inflater.inflate(R.layout.bookmarks_fragment, container, false);
			mBookmarksGrid = (GridView) mContainer.findViewById(R.id.BookmarksGridView);
		}
		
		return mContainer;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		long id = ((AdapterContextMenuInfo) menuInfo).id;
		if (id != -1) {
			BookmarkHistoryItem selectedItem = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), id);
			if (selectedItem != null) {
				BitmapDrawable icon = ApplicationUtils.getApplicationButtonImage(getActivity(), selectedItem.getFavicon());
				if (icon != null) {
					menu.setHeaderIcon(icon);
				}
				
				menu.setHeaderTitle(selectedItem.getTitle());
				
				menu.add(0, CONTEXT_MENU_OPEN_IN_TAB, 0, R.string.OpenInTab);
				menu.add(0, CONTEXT_MENU_EDIT_BOOKMARK, 0, R.string.EditBookmark);
		        menu.add(0, CONTEXT_MENU_COPY_URL, 0, R.string.CopyUrl);
		        menu.add(0, CONTEXT_MENU_SHARE_URL, 0, R.string.ContextMenuShareUrl);
		        menu.add(0, CONTEXT_MENU_DELETE_BOOKMARK, 0, R.string.DeleteBookmark);
		        
		        List<AddonMenuItem> addonsContributions = Controller.getInstance().getAddonManager().getContributedBookmarkContextMenuItems();
		        for (AddonMenuItem item : addonsContributions) {
		        	menu.add(0, item.getAddon().getMenuId(), 0, item.getMenuItem());
		        }
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		
		BookmarkHistoryItem selectedItem = BookmarksWrapper.getBookmarkById(getActivity().getContentResolver(), info.id);
		
		Intent i;
		switch (item.getItemId()) {
		case CONTEXT_MENU_OPEN_IN_TAB:			
			if (item != null) {
				Intent result = new Intent();
				result.putExtra(Constants.EXTRA_NEW_TAB, true);
				result.putExtra(Constants.EXTRA_URL, selectedItem.getUrl());
				
				getActivity().setResult(Activity.RESULT_OK, result);
				getActivity().finish();
			}
			return true;
			
		case CONTEXT_MENU_EDIT_BOOKMARK:
			if (selectedItem != null) {
				i = new Intent(getActivity(), EditBookmarkActivity.class);
				i.putExtra(Constants.EXTRA_ID, info.id);
				i.putExtra(Constants.EXTRA_LABEL, selectedItem.getTitle());
				i.putExtra(Constants.EXTRA_URL, selectedItem.getUrl());
				
				startActivity(i);
			}
			
			return true;

		case CONTEXT_MENU_COPY_URL:
			if (selectedItem != null) {
				ApplicationUtils.copyTextToClipboard(getActivity(), selectedItem.getUrl(), getActivity().getResources().getString(R.string.UrlCopyToastMessage));
			}
			
			return true;
			
		case CONTEXT_MENU_SHARE_URL:
			if (selectedItem != null) {
				ApplicationUtils.sharePage(getActivity(), null, selectedItem.getUrl());						
			}
			
			return true;
			
		case CONTEXT_MENU_DELETE_BOOKMARK:
			BookmarksWrapper.deleteBookmark(getActivity().getContentResolver(), info.id);
			return true;
		
		default:
			if (Controller.getInstance().getAddonManager().onContributedBookmarkContextMenuItemSelected(
					getActivity(),
					item.getItemId(),
					selectedItem.getTitle(),
					selectedItem.getUrl(),
					mUIManager.getCurrentWebView())) {
				return true;
			} else {
				return super.onContextItemSelected(item);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return BookmarksWrapper.getCursorLoaderForBookmarks(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

}
