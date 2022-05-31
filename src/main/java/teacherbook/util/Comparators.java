package teacherbook.util;

import teacherbook.model.gradebook.GradebookEntry;

import java.util.Comparator;

public class Comparators {
    public Comparator<GradebookEntry> compareGEByDate = new Comparator<GradebookEntry>() {
        @Override
        public int compare(GradebookEntry o1, GradebookEntry o2) {
            return o1.getAssignment().getDay().getDate().compareTo(
                    o2.getAssignment().getDay().getDate());
        }
    };
}
