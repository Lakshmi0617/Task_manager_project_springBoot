package com.tasktracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class UIController {

    @Autowired
    private TaskRepository repo;

    @GetMapping("/")
    public String dashboard(Model model) {

        // Get all tasks and sort by ID to maintain consistent order
        List<Task> tasks = repo.findAll();
        tasks.sort(Comparator.comparing(Task::getId));
        
        model.addAttribute("tasks", tasks);
        return "dashboard";
    }
}
