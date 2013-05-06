package com.mastertechsoftware.ui;

import android.R;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.ViewFlipper;

import com.mastertechsoftware.util.log.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
/**
 * Handler for Fragments and a ViewPager
 */
public class FragmentHandler {
	public enum StackName {
		KIDS,
		RULES,
		REWARDS,
		MORE
	}
	protected Map<StackName, Stack<FragmentEntry>> fragmentStacks = new HashMap<StackName, Stack<FragmentEntry>>();
	protected FragmentListener fragmentListener;
	protected FragmentManager mFragmentManager;
	protected int contentId;

	/**
	 * Constructor.
	 * @param fragmentManager
	 * @param listener
	 */
	public FragmentHandler(int id, FragmentManager fragmentManager,  FragmentListener listener) {
		contentId = id;
		this.mFragmentManager = fragmentManager;
		this.fragmentListener = listener;
	}
	/**
	 * Select the given fragment
	 * @param stackName
	 * @param fragmentName
	 */
	public void selectFragment(StackName stackName, String fragmentName) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		Logger.debug(this, "selectFragment: " + fragmentName + " has " + fragmentEntries.size() + " fragmentEntries items");
		FragmentEntry fragmentEntry = getFragmentEntry(stackName, fragmentName);
		if (fragmentEntry != null) {
			Logger.debug(this, "selectFragment Found fragment at " + fragmentEntry.getFragmentPosition());
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			replaceFragment(fragmentEntry, transaction);
			transaction.commitAllowingStateLoss();
//			FragmentTransaction transaction = mFragmentManager.beginTransaction();
//			replaceFragment(fragmentEntry, transaction);
//			clearFragmentStack(transaction);
//			addFragment(stackName, fragmentEntry.getName(), fragmentEntry.fragment);
//			viewFlipper.setDisplayedChild(fragmentEntry.getFragmentPosition());
//			viewPager.setCurrentItem(fragmentEntry.getFragmentPosition());
		}
	}

	public void selectTopFragment(StackName stackName) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries == null) {
			Logger.error(this, "selectTopFragment no entries for " + stackName);
			return;
		}
		if (!fragmentEntries.empty()) {
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			replaceFragment(fragmentEntries.peek(), transaction);
			transaction.commitAllowingStateLoss();
		}
	}

	public Fragment getNextAvailableFragment() {
		for (StackName stackName : fragmentStacks.keySet()) {
			Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
			if (fragmentEntries == null) {
				continue;
			}
			if (!fragmentEntries.empty()) {
				return fragmentEntries.peek().fragment;
			}
		}
		return null;
	}

	public String getFragmentName(Fragment fragment) {
		for (StackName stackName : fragmentStacks.keySet()) {
			Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
			if (fragmentEntries == null) {
				continue;
			}
			if (!fragmentEntries.empty()) {
				for (FragmentEntry fragmentEntry : fragmentEntries) {
					if (fragmentEntry.fragment == fragment) {
						return fragmentEntry.getName();
					}
				}
			}
		}
		return null;
	}

	public StackName getFragmentStackName(Fragment fragment) {
		for (StackName stackName : fragmentStacks.keySet()) {
			Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
			if (fragmentEntries == null) {
				continue;
			}
			if (!fragmentEntries.empty()) {
				for (FragmentEntry fragmentEntry : fragmentEntries) {
					if (fragmentEntry.fragment == fragment) {
						return stackName;
					}
				}
			}
		}
		return null;
	}

	public Fragment getFragment(StackName stackName, int position) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries == null) {
			return null;
		}
		if (position < fragmentEntries.size()) {
			return fragmentEntries.get(position).getFragment();
		}
		return null;
	}

	public Fragment getFragment(StackName stackName, String fragmentName) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries == null) {
			return null;
		}
		for (FragmentEntry fragmentEntry : fragmentEntries) {
			if (fragmentName.equalsIgnoreCase(fragmentEntry.getName())) {
				return fragmentEntry.getFragment();
			}
		}
		return null;
	}

	protected FragmentEntry getFragmentEntry(StackName stackName, String fragmentName) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries == null) {
			return null;
		}
		for (FragmentEntry fragmentEntry : fragmentEntries) {
			if (fragmentName.equalsIgnoreCase(fragmentEntry.getName())) {
				return fragmentEntry;
			}
		}
		return null;
	}

	public void removeFragment(StackName stackName, Fragment fragment) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries == null) {
			return;
		}
		for (FragmentEntry fragmentEntry : fragmentEntries) {
			if (fragment == fragmentEntry.getFragment()) {
				fragmentEntries.remove(fragmentEntry);
				return;
			}
		}
	}


	/**
	 * Go back a page
	 * @return true if we have items to go back to
	 */
	public boolean goBack() {
		if (mFragmentManager.getBackStackEntryCount() > 0) {
			mFragmentManager.popBackStackImmediate();
			return true;
		}
		return false;
	}

	public int stackSize(StackName stackName) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries == null) {
			return 0;
		}
		return fragmentEntries.size();
	}

	/**
	 * Add a new fragment for this tab type
	 * @param stackName
	 * @param fragmentName
	 * @param fragment
	 */
	public void addFragment(StackName stackName, String fragmentName, Fragment fragment) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries == null) {
			fragmentEntries = new Stack<FragmentEntry>();
			fragmentStacks.put(stackName, fragmentEntries);
		}
		//		Logger.debug(this, "addFragment:  " + fragmentName + " Fragment " + fragment.getClass().getSimpleName());

//		String randomTag = UUID.randomUUID().toString();
		FragmentEntry stackEntry = new FragmentEntry(fragment, fragmentEntries.size(), fragmentName);
		fragmentEntries.push(stackEntry);
		Logger.debug(this, "addFragment: " + fragmentName + " has " + fragmentEntries.size() + " fragmentEntries items");
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		ft.replace(contentId, fragment, fragmentName);
//		ft.add(contentId, fragment, fragmentName);
		ft.commitAllowingStateLoss();
//		Logger.debug(this, "addFragment: Tag is " + randomTag);
	}

	/**
	 * Pop the top Fragment for this tab type
	 * @param stackName
	 * @return Fragment
	 */
	public Fragment popTopFragmentStack(StackName stackName) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries != null && fragmentEntries.size() > 0) {
			Logger.debug(this, "popTopFragmentStack: fragmentEntries of size " + fragmentEntries.size() + " top fragmentEntries item is " + fragmentEntries.peek().getName());
			FragmentEntry stackEntry = fragmentEntries.pop();
			Logger.debug(this,
				"popTopFragmentStack: popping " + stackEntry.getName() + " of type " + stackEntry.fragment.getClass().getSimpleName());
			FragmentTransaction transaction = mFragmentManager.beginTransaction();
			if (!fragmentEntries.empty()) {
				replaceFragment(fragmentEntries.peek(), transaction);
			} else {
				transaction.remove(stackEntry.fragment);
			}
			transaction.commitAllowingStateLoss();
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
		transaction.replace(contentId, fragment, getFragmentName(fragment));
//		transaction.show(fragment);
		transaction.commitAllowingStateLoss();
	}

	public void clearFragmentStack(StackName stackName) {
		FragmentTransaction transaction = mFragmentManager.beginTransaction();
		clearFragmentStack(stackName, transaction);
		transaction.commitAllowingStateLoss();
	}

	/**
	 * Clear the stack for this tab type
	 * @param stackName
	 * @param transaction
	 */
	public void clearFragmentStack(StackName stackName, FragmentTransaction transaction) {
		if (transaction == null) {
			return;
		}
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
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

	public Fragment getCurrentFragment(StackName stackName) {
		Stack<FragmentEntry> fragmentEntries = fragmentStacks.get(stackName);
		if (fragmentEntries.empty()) {
			return null;
		}
		FragmentEntry stackEntry = fragmentEntries.peek();
		return stackEntry.getFragment();
	}
}
