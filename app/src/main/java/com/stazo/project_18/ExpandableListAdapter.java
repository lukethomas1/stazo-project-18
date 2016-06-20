package com.stazo.project_18;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Author: Brian Chan
 * Date: 5/13/2016
 * Description: This class is for the dynamically changing list view for events for
 * stazo-project-18.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> headerList; // Header Titles
    private HashMap<String, ArrayList<Event>> headerToEventListHM; // Child text

    public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                 HashMap<String, ArrayList<Event>> listChildData) {
        this._context = context;
        this.headerList = listDataHeader;
        this.headerToEventListHM = listChildData;
    }

    @Override
    /**
     * Returns the event which corresponds to a child
     */
    public Object getChild(int groupPosition, int childPosition) {
        return this.headerToEventListHM.get(this.headerList.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = ((Event) getChild(groupPosition, childPosition)).getName();

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_activity_event_details, null);
        }

        TextView txtListChild = (TextView) convertView.findViewById(R.id.eventDetailsList);

        txtListChild.setTypeface(null, Typeface.ITALIC);
        txtListChild.setTextColor(Color.GRAY);

        txtListChild.setText(childText);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return headerToEventListHM.get(headerList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headerList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return headerList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        // FIXME Temporary workaround for "Lit" title
    //    if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


            if (groupPosition == 0) {
                convertView = inflater.inflate(R.layout.group_title, null);
            }
            else {
                convertView = inflater.inflate(R.layout.list_activity_event_name, null);
            }
      //  }

        convertView.setBackgroundColor(Color.WHITE);

        // FIXME Same as above
        if (groupPosition == 0) {
            ImageView textImg = (ImageView) convertView.findViewById(R.id.groupTextImage);
            textImg.setImageResource(R.drawable.lit_events);
        }

       else {
            TextView lblListHeader = (TextView) convertView.findViewById(R.id.eventNamesList);

            lblListHeader.setTextColor(Color.DKGRAY);

            lblListHeader.setText(headerTitle);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public String getEventId(int groupPosition, int childPosition) {
        // Get the list of events under a group
        ArrayList<Event> eventList = headerToEventListHM.get((String) getGroup(groupPosition));

        return eventList.get(childPosition).getEvent_id();
    }
}
