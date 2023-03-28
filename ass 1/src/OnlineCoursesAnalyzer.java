import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.toMap;

/**
 *
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> result = new TreeMap<>(String::compareTo);
        for(Course course:courses){
            if(result.containsKey(course.institution)){
                result.replace(course.institution, result.get(course.institution)+course.participants);
            }
            else {
                result.put(course.institution,course.participants);
            }
        }
        return result;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> map = new HashMap<>();
        for(Course course:courses) {
            if (map.containsKey(course.institution + '-' + course.subject)) {
                map.replace(course.institution + '-' + course.subject, map.get(course.institution + '-' + course.subject) + course.participants);
            } else {
                map.put(course.institution + '-' + course.subject, course.participants);
            }
        }
        return map.entrySet().stream()
                .sorted(((o1, o2) -> !Objects.equals(o1.getValue(), o2.getValue()) ? o2.getValue()-o1.getValue():o1.getKey().compareTo(o2.getKey())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> result = new LinkedHashMap<>();
        for (Course course:courses){
            String[] instructors = course.instructors.replace("\"","").split(", ");
            if(instructors.length == 1){
                if(result.containsKey(instructors[0])){
                    if(!result.get(instructors[0]).get(0).contains(course.title)) {
                        result.get(instructors[0]).get(0).add(course.title);
                    }
                    result.get(instructors[0]).get(0).sort(String::compareTo);
                }
                else {
                    List<String> tem1 = new ArrayList<>();
                    tem1.add(course.title);
                    List<String> tem2 = new ArrayList<>();
                    List<List<String>> tem = new ArrayList<>();
                    tem.add(0,tem1);
                    tem.add(1,tem2);
                    result.put(instructors[0],tem);
                }
            }
            else {
                for (String instructor:instructors){
                    if(result.containsKey(instructor)){
                        if(!result.get(instructor).get(1).contains(course.title)) {
                            result.get(instructor).get(1).add(course.title);
                        }
                        result.get(instructor).get(1).sort(String::compareTo);
                    }
                    else {
                        List<String> tem1 = new ArrayList<>();
                        List<String> tem2 = new ArrayList<>();
                        tem2.add(course.title);
                        List<List<String>> tem = new ArrayList<>();
                        tem.add(0,tem1);
                        tem.add(1,tem2);
                        result.put(instructor,tem);
                    }
                }
            }
        }
        return result;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<String> result = new ArrayList<>();
        Comparator<Course> comparator1 = (o1, o2) -> o1.totalHours != o2.totalHours ? (int)(o2.totalHours-o1.totalHours) : o1.title.compareTo(o2.title);
        Comparator<Course> comparator2 = (o1, o2) -> o1.participants != o2.participants ? o2.participants-o1.participants : o1.title.compareTo(o2.title);
        switch (by) {
            case "hours" -> courses.sort(comparator1);
            case "participants" -> courses.sort(comparator2);
        }
        for(int i = 0;i < topK;i++){
            if(result.contains(courses.get(i).title)){
                topK++;
            }
            else {
                result.add(courses.get(i).title);
            }
        }
        return result;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<String> result = new ArrayList<>();
        for(Course course:courses){
            if(course.subject.toLowerCase().contains(courseSubject.toLowerCase()) && course.percentAudited >= percentAudited
                    && course.totalHours <= totalCourseHours && !result.contains(course.title)){
                result.add(course.title);
            }
        }
        result.sort(String::compareTo);
        return result;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        List<String> result = new ArrayList<>();
        Map<String, AveCourse> map = new HashMap<>();
        for(Course course:courses){
            if(map.containsKey(course.number)){
                map.get(course.number).count++;
                map.get(course.number).totalAge += course.medianAge;
                map.get(course.number).totalMale += course.percentMale;
                map.get(course.number).totalDegree += course.percentDegree;
                if(course.launchDate.after(map.get(course.number).date)){
                    map.get(course.number).date = course.launchDate;
                    map.get(course.number).title = course.title;
                }
            }
            else {
                AveCourse aveCourse = new AveCourse();
                aveCourse.count = 1;
                aveCourse.totalAge = course.medianAge;
                aveCourse.totalMale = course.percentMale;
                aveCourse.totalDegree = course.percentDegree;
                aveCourse.date = course.launchDate;
                aveCourse.title = course.title;
                map.put(course.number, aveCourse);
            }
        }
        for (String key:map.keySet()){
            AveCourse value = map.get(key);
            value.similarity = (age-value.totalAge/value.count)*(age-value.totalAge/value.count)
                    +(gender*100-value.totalMale/value.count)*(gender*100-value.totalMale/value.count)
                    +(isBachelorOrHigher*100-value.totalDegree/value.count)*(isBachelorOrHigher*100-value.totalDegree/value.count);
        }
        Map<String,AveCourse> sorted = map.entrySet().stream()
                .sorted(((o1, o2) -> !Objects.equals(o1.getValue().similarity, o2.getValue().similarity)
                        ? (int) (o1.getValue().similarity - o2.getValue().similarity)
                        : o1.getValue().title.compareTo(o2.getValue().title)))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));
        int i = 0;
        for (Map.Entry<String, AveCourse> entry : sorted.entrySet()) {
            if (!result.contains(entry.getValue().title)) {
                result.add(entry.getValue().title);
                i++;
            }
            if (i >= 10) {
                break;
            }
        }
        return result;
    }

}

class AveCourse {
    int count;
    double totalAge;
    double totalMale;
    double totalDegree;
    String title;
    Date date;
    double similarity;
    public AveCourse(){}
}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}