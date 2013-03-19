package com.mastertechsoftware.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.ArrayList;
import java.util.List;
/**
 * Adapter for Fragments
 */
public class FragmentAdapter extends FragmentStatePagerAdapter {
	protected List<Fragment> fragments = new ArrayList<Fragment>();

	public FragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	public void addFragment(Fragment fragment, int position) {
		fragments.add(position, fragment);
		notifyDataSetChanged();
	}

	public void replaceFragment(Fragment fragment, int position) {
		fragments.remove(position);
		fragments.add(position, fragment);
		notifyDataSetChanged();
	}

	public void removeFragment(Fragment fragment) {
		fragments.remove(fragment);
		notifyDataSetChanged();
	}

	public void removeFragment(int position) {
		fragments.remove(position);
		notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

	@Override
	public int getItemPosition(Object object){
		return PagerAdapter.POSITION_NONE;
	}
}
