package com.mastertechsoftware.ui;

import android.support.v4.app.Fragment;
/**
 *
 */
public class FragmentEntry {
	protected int fragmentPosition;
	protected String name;
	protected Fragment fragment;

	public FragmentEntry(Fragment fragment, int fragmentPosition, String name) {
		this.fragment = fragment;
		this.fragmentPosition = fragmentPosition;
		this.name = name;
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public int getFragmentPosition() {
		return fragmentPosition;
	}

	public void setFragmentPosition(int fragmentPosition) {
		this.fragmentPosition = fragmentPosition;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
