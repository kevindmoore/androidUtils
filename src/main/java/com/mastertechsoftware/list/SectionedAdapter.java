/***
 Copyright (c) 2008-2010 CommonsWare, LLC
 portions Copyright (c) 2008-10 Jeffrey Sharkey

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.	If not, see <http://www.gnu.org/licenses/>.
 */

package com.mastertechsoftware.list;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

abstract public class SectionedAdapter extends BaseAdapter {

    abstract protected View getHeaderView(String caption,
                                          int index,
                                          View convertView,
                                          ViewGroup parent);

    private List<Section> sections = new ArrayList<Section>();
    private static int TYPE_SECTION_HEADER = 0;
    private static int TYPE_SECTION = 1;

    public SectionedAdapter() {
        super();
    }

    public Section addSection(String caption, Adapter adapter) {
        Section section = new Section(caption, adapter);
        sections.add(section);
        notifyDataSetChanged();
        return section;
    }

    public Object getItem(int position) {
        for (Section section : this.sections) {
            if (position == 0) {
                return (section);
            }

            if (section.isCollapsed()) {
                position -= 1;
                continue;
            }
            int size = section.adapter.getCount();
            if (!(section.adapter instanceof SectionedAdapter)) {
                size += 1;
            }

            if (position < size) {
                return (section.adapter.getItem(position - 1));
            }

            position -= size;
        }

        return (null);
    }

    public Section getSection(int position) {
        for (Section section : this.sections) {
            if (position == 0) {
                return (section);
            }

            int size = section.adapter.getCount();
            if (!(section.adapter instanceof SectionedAdapter)) {
                size += 1;
            }
            if (position < size) {
                return section;
            }

            position -= size;
        }
        return null;
    }

    public Section getSectionAtIndex(int index) {
        if (index < sections.size()) {
            return sections.get(index);
        }
        return null;
    }

    public int getCount() {
        int total = 0;

        for (Section section : this.sections) {
            if (!section.isCollapsed()) {
                total += section.adapter.getCount();  
                if (!(section.adapter instanceof SectionedAdapter)) {
                    total += 1; // 1 for header
                }
            } else {
                total += 1; // 1 for header
            }
        }

        return total;
    }

    public int getViewTypeCount() {
        if (getCount() == 0) {
            return 1;
        }
        return getCount();
//        int total = 1;    // one for the header, plus those from sections
//
//        for (Section section : this.sections) {
//            if (section.isCollapsed()) {
//                continue;
//            }
//            total += section.adapter.getViewTypeCount();
//        }
//
//        return total;
    }

    public int getItemViewType(int position) {
        int typeOffset = TYPE_SECTION_HEADER + 1;    // start counting from here

        for (Section section : this.sections) {
            if (position == 0) {
                return (TYPE_SECTION_HEADER);
            }

            if (section.isCollapsed()) {
                position -= 1;
                continue;
            }
            int size = section.adapter.getCount();
            if (!(section.adapter instanceof SectionedAdapter)) {
                size += 1; // 1 for header
            }

            if (position < size) {
                return (typeOffset + section.adapter.getItemViewType(position - 1));
            }

            position -= size;
            typeOffset += section.adapter.getViewTypeCount();
        }

        return (-1);
    }

    public boolean areAllItemsSelectable() {
        return (false);
    }

    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    @Override
    public View getView(int position, View convertView,
                        ViewGroup parent) {
        int sectionIndex = 0;

        for (Section section : this.sections) {
            if (position == 0) {
                return (getHeaderView(section.caption, sectionIndex,
                        convertView, parent));
            }

            if (section.isCollapsed()) {
                position -= 1;
                sectionIndex++;
                continue;
            }

            int size = section.adapter.getCount();
            if (!(section.adapter instanceof SectionedAdapter)) {
                size += 1;
            }

            if (position < size) {
                return (section.adapter.getView(position - 1,
                        convertView,
                        parent));
            }

            position -= size;
            sectionIndex++;
        }

        return (null);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    public void removeAll() {
        this.sections.clear();
        notifyDataSetChanged();
    }

    public class Section {
        protected String caption;
        protected Adapter adapter;
        protected boolean collapsed = false;

        Section(String caption, Adapter adapter) {
            this.caption = caption;
            this.adapter = adapter;
        }

        public Adapter getAdapter() {
            return adapter;
        }

        public String getCaption() {
            return caption;
        }

        public boolean isCollapsed() {
            return collapsed;
        }

        public boolean subSectionsCollapsed() {
            if (!(adapter instanceof SectionedAdapter)) {
                return false;
            }
            boolean collapsed = true;
            int count = ((SectionedAdapter)adapter).sections.size();
            for (int i=0; i < count; i++) {
                Section item = ((SectionedAdapter)adapter).getSectionAtIndex(i);
                if (item != null && !item.isCollapsed()) {
                   return false;
                }
            }
            return collapsed;
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
        }
    }
}