package com.example.diploma;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.diploma.model.Task;

import java.util.List;

public class TaskAdapter extends ArrayAdapter<Task> {
    private LayoutInflater inflater;
    private int layout;
    private List<Task> taskList;

    public TaskAdapter(Context context, int resource, List<Task> taskList){
        super(context, resource, taskList);
        this.taskList = taskList;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //Полученный объект
        Task task = taskList.get(position);

        //Значения из полученного объекта
        viewHolder.name.setText(task.getName());
        viewHolder.status.setText(task.getStatusId().getValue());
        viewHolder.planned.setText(String.valueOf("Planned: " + task.getPlannedTime().getHours() + ":" + task.getPlannedTime().getMinutes()));
        viewHolder.deadline.setText(String.valueOf("Deadline: " + Utils.prettyDate(Utils.convertToLocalDateViaInstant(task.getDeadlineTime())) + " " + task.getDeadlineTime().getHours() + ":" + task.getDeadlineTime().getMinutes()));
        viewHolder.priority.setText(task.getPriorityId().getValue());
        viewHolder.desc.setText(task.getDesc());
        viewHolder.category.setText(task.getCategoryId().getName());
        viewHolder.verticalLine.setBackgroundColor(Color.parseColor(task.getCategoryId().getColour()));

        return convertView;
    }

    private static class ViewHolder {
        //Чтобы не искать каждый раз по id, в случаях когда контент вью уже определён
        final TextView name;
        final TextView priority;
        final TextView desc;
        final TextView deadline;
        final TextView planned;
        final TextView status;
        final TextView category;
        public View verticalLine;
        ViewHolder(View view){
            name = view.findViewById(R.id.name);
            deadline = view.findViewById(R.id.deadlineTime);
            desc = view.findViewById(R.id.desc);
            planned = view.findViewById(R.id.plannedTime);
            status = view.findViewById(R.id.status);
            priority = view.findViewById(R.id.priority);
            category = view.findViewById(R.id.category);
            verticalLine = view.findViewById(R.id.verticalLine);
        }
    }

}
