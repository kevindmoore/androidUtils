package com.mastertechsoftware.ui;

import android.R;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.ViewFlipper;

import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
/**
 * Handler for Fragments and a ViewPager
 */
public class FragmentHandler {
	protected Stack<FragmentEntry> fragmentEntries = new Stack<FragmentEntry>();
	//	protected ViewPager viewPager;
	protected ViewFlipper viewFlipper;
	protected int currentFragmentPosition = -1;
//	protected FragmentAdapter adapter;
	protected FragmentListener fragmentListener;
	protected FragmentManager mFragmentManager;
	protected int contentId;

	/**
	 * Constructor.
	 * @param fragmentManager
	 * @param viewFlipper
	 * @param listener
	 */
//	public FragmentHandler(FragmentAdapter adapter, ViewPager viewPager, FragmentListener listener) {
	public FragmentHandler(int id, FragmentManager fragmentManager, ViewFlipper viewFlipper, FragmentListener listener) {
		contentId = id;
		this.mFragmentManager = fragmentManager;
//		this.adapter = adapter;
		this.viewFlipper = viewFlipper;
//		this.viewPager = viewPager;
		this.fragmentListener = listener;
//		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//			@Override
//			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			}
//
//			@Override
//			public void onPageSelected(int position) {
//				Logger.debug(this, "onPageSelected position " + position + " currentFragmentPosition " + currentFragmentPosition);
//				currentFragmentPosition = position;
//				if (fragmentListener != null) {
//					fragmentListener.pageChanged(position, getFragment(position));
//				}
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int state) {
//			}
//		});
	}
	/**
	 * Select the given fragment
	 * @param fragmentName
	 */
	public void selectFragment(String fragmentName) {
		FragmentEntry fragmentEntry = getFragmentEntry(fragmentName);
		if (fragmentEntry != null) {
			Logger.debug(this, "addFragment Found fragment at " + fragmentEntry.getFragmentPosition());
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			clearFragmentStack(transaction);
			addFragment(fragmentEntry.getName(), fragmentEntry.fragment);
//			viewFlipper.setDisplayedChild(fragmentEntry.getFragmentPosition());
//			viewPager.setCurrentItem(fragmentEntry.getFragmentPosition());
		}
	}

	public Fragment getFragment(int position) {
		if (position < fragmentEntries.size()) {
			return fragmentEntries.get(position).getFragment();
		}
		return null;
	}

	protected Fragment getFragment(String fragmentName) {
		for (FragmentEntry fragmentEntry : fragmentEntries) {
			if (fragmentName.equalsIgnoreCase(fragmentEntry.getName())) {
				return fragmentEntry.getFragment();
			}
		}
		return null;
	}

	protected FragmentEntry getFragmentEntry(String fragmentName) {
		for (FragmentEntry fragmentEntry : fragmentEntries) {
			if (fragmentName.equalsIgnoreCase(fragmentEntry.getName())) {
				return fragmentEntry;
			}
		}
		return null;
	}

	public void removeFragment(Fragment fragment) {
		for (FragmentEntry fragmentEntry : fragmentEntries) {
			if (fragment == fragmentEntry.getFragment()) {
				fragmentEntries.remove(fragmentEntry);
//				adapter.destroyItem(viewPager, fragmentEntry.getFragmentPosition(), fragmentEntry.getFragment());
//				adapter.removeFragment(fragmentEntry.getFragmentPosition());
//				adapter.finishUpdate(viewPager);
				return;
			}
		}
	}

/*
	*/
/**
	 * Add a new fragment for this tab type
	 * @param fragmentName
	 * @param fragment
	 *//*

	public void addFragment(String fragmentName, Fragment fragment) {
		currentFragmentPosition++;
		Logger.debug(this, "addFragment " + fragmentName + " currentFragmentPosition " + currentFragmentPosition);
		FragmentEntry fragmentEntry = getFragmentEntry(fragmentName);
*/
/*
		int count = adapter.getCount();
		if (count > currentFragmentPosition) {
			Logger.debug(this, "addFragment count " + count + " > currentFragmentPosition " + currentFragmentPosition + " destroying");
			for (int i = count-1; i > currentFragmentPosition; i--) {
				Logger.debug(this, "addFragment deleting fragment " + i);
//				Fragment currentFragment = adapter.getItem(i);
//				adapter.destroyItem(viewPager, i, currentFragment);
				adapter.removeFragment(i);
			}
//			adapter.finishUpdate(viewPager);
		}
*//*

		if (fragmentEntry == null) {
			fragmentEntry = new FragmentEntry(fragment, currentFragmentPosition, fragmentName);
			fragmentEntries.add(fragmentEntry);
		}
*/
/*
		if (fragmentEntry != null) {
			Logger.debug(this, "addFragment Found fragment at " + fragmentEntry.getFragmentPosition());
			if (viewFlipper.getChildCount() > fragmentEntry.getFragmentPosition()) {
				viewFlipper.setDisplayedChild(fragmentEntry.getFragmentPosition());
				return;
			}
//			if (viewPager.getChildCount() > fragmentEntry.getFragmentPosition()) {
//				viewPager.setCurrentItem(fragmentEntry.getFragmentPosition());
//				return;
//			}
			Logger.debug(this, "addFragment Couldn't set current item for  " + fragmentName);
			return;
		}
*//*

//		adapter.addFragment(fragment, currentFragmentPosition);
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		//		ft.add(R.id.content, fragment, stackName);
		ft.replace(contentId, fragment, fragmentName);
		ft.addToBackStack(fragmentName);
		ft.commit();
//		adapter.instantiateItem(viewPager, currentFragmentPosition);
//		adapter.finishUpdate(viewPager);
//		viewPager.setCurrentItem(currentFragmentPosition);
//		viewFlipper.setDisplayedChild(currentFragmentPosition);
		viewFlipper.setDisplayedChild(viewFlipper.getChildCount()-1);
	}
*/

	/**
	 * Go back a page
	 * @return true if we have items to go back to
	 */
	public boolean goBack() {
		if (mFragmentManager.getBackStackEntryCount() > 0) {
			--currentFragmentPosition;
			mFragmentManager.popBackStackImmediate();
			return true;
		}
		//		if (viewFlipper.getDisplayedChild() > 0) {
		//			--currentFragmentPosition;
		//			Logger.debug(this, "goBack currentFragmentPosition " + currentFragmentPosition + " viewPage current item " + viewFlipper.getDisplayedChild());
		//			viewFlipper.setDisplayedChild(viewFlipper.getDisplayedChild() - 1);
		//			return true;
		//
		//		}
		//		if (viewPager.getCurrentItem() > 0) {
		//			--currentFragmentPosition;
		//			Logger.debug(this, "goBack currentFragmentPosition " + currentFragmentPosition + " viewPage current item " + viewPager.getCurrentItem());
		//			viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
		//			return true;
		//		}
		return false;
	}

	public int stackSize() {
		return fragmentEntries.size();
	}

	/**
	 * Add a new fragment for this tab type
	 * @param fragmentName
	 * @param fragment
	 */
	public void addFragment(String fragmentName, Fragment fragment) {
		Logger.debug(this, "addFragment:  " + fragmentName + " Fragment " + fragment.getClass().getSimpleName());

		currentFragmentPosition++;
//		String randomTag = UUID.randomUUID().toString();
		FragmentEntry stackEntry = new FragmentEntry(fragment, currentFragmentPosition, fragmentName);
		fragmentEntries.push(stackEntry);
		Logger.debug(this, "addFragment: " + fragmentName + " has " + fragmentEntries.size() + " fragmentEntries items");
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.replace(contentId, fragment, fragmentName);
//		ft.add(contentId, fragment, fragmentName);
		ft.commit();
//		Logger.debug(this, "addFragment: Tag is " + randomTag);
	}

	/**
	 * Pop the top Fragment for this tab type
	 * @return Fragment
	 */
	public Fragment popTopFragmentStack() {
		if (fragmentEntries != null && fragmentEntries.size() > 0) {
			Logger.debug(this, "popTopFragmentStack: Found fragmentEntries of size " + fragmentEntries.size() + " top fragmentEntries item is " + fragmentEntries.peek().getName());
			FragmentEntry stackEntry = fragmentEntries.pop();
			Logger.debug(this,
				"popTopFragmentStack: popping " + stackEntry.getName() + " of type " + stackEntry.fragment.getClass().getSimpleName());
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			transaction.remove(stackEntry.fragment);
			if (!fragmentEntries.empty()) {
				replaceFragment(fragmentEntries.peek(), transaction);
			}
			transaction.commit();
			return stackEntry.fragment;
		}
		return null;
	}

	protected void replaceFragment(FragmentEntry fragmentEntry, FragmentTransaction transaction) {
		transaction.replace(contentId, fragmentEntry.fragment, fragmentEntry.getName());
	}

	/**
	 * Show the given fragment
	 * @param fragment
	 */
	public void showFragment(Fragment fragment) {
		Logger.debug(this, "showFragment: fragment " + fragment.getClass().getSimpleName());
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		transaction.show(fragment);
		transaction.commit();
	}

	/**
	 * Clear the stack for this tab type
	 * @param transaction
	 */
	public void clearFragmentStack(FragmentTransaction transaction) {
		if (transaction == null) {
			return;
		}
		if (fragmentEntries != null) {
			Logger.debug(this, "clearFragmentStack: Found stack of size " + fragmentEntries.size());
			while (!fragmentEntries.empty()) {
				FragmentEntry stackEntry = fragmentEntries.pop();
				Logger.debug(this,
					"clearFragmentStack: popping tag " + stackEntry.getName() + " of type " + stackEntry.fragment.getClass().getSimpleName());
				transaction.remove(stackEntry.fragment);
			}
			fragmentEntries.clear();
		}
	}

	public Fragment getCurrentFragment() {
		if (fragmentEntries.empty()) {
			return null;
		}
		FragmentEntry stackEntry = fragmentEntries.pop();
		return stackEntry.getFragment();
	}
}
