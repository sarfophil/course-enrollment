package com.group3.courseenrollment.Service;

<<<<<<< HEAD
import java.util.List;

import com.group3.courseenrollment.domain.Course;

public interface CourseService {

	public Course addCourse(Course course);

	public List<Course> viewCourses();

	public Course viewCourseById(Long course_id);

}
=======

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.group3.courseenrollment.NoSuchResourceException;
import com.group3.courseenrollment.domain.Course;
import com.group3.courseenrollment.repository.CourseRepository;

import java.util.List;

public class CourseService {
    @Autowired
    CourseRepository courseRepository;

    @Transactional(propagation =Propagation.REQUIRES_NEW)
    public List<Course> getAllCourses(){
        return courseRepository.findAll();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Course addCourse(Course course){
        return courseRepository.save(course);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Course getCourse(long courseId) throws NoSuchResourceException{
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchResourceException("Can't find Course", courseId));
        return course;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Course updateCourse(long courseId, Course new_Course) throws NoSuchResourceException{
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchResourceException("Can't find course", courseId));
        course.setCode(new_Course.getCode());
        course.setName(new_Course.getName());
        course.setDescription(new_Course.getDescription());

        final Course updated_Course = courseRepository.save(course);

        return updated_Course;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseEntity<Void> deleteCourse(long courseId) throws NoSuchResourceException{
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchResourceException("Cant find course", courseId));
        courseRepository.delete(course);
        return  ResponseEntity.noContent().build();
}
}

>>>>>>> 320d43b90ab6ffc142324f61294b12e22dc39f7b
