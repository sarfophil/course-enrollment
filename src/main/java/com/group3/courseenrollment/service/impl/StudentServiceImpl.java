package com.group3.courseenrollment.service.impl;

import com.group3.courseenrollment.configuration.ApplicationProperties;
import com.group3.courseenrollment.domain.Enrollment;
import com.group3.courseenrollment.domain.Entry;
import com.group3.courseenrollment.domain.Section;
import com.group3.courseenrollment.domain.Student;
import com.group3.courseenrollment.dto.StudentEnrollmentDto;
import com.group3.courseenrollment.exception.EnrollmentLimitExceededException;
import com.group3.courseenrollment.exception.HasNoWriteException;
import com.group3.courseenrollment.repository.EnrollmentRepository;
import com.group3.courseenrollment.repository.EntryRepository;
import com.group3.courseenrollment.repository.SectionRepository;
import com.group3.courseenrollment.repository.StudentRepository;
import com.group3.courseenrollment.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class StudentServiceImpl implements StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private EnrollmentRepository enrollmentRepository;


    @Secured("ROLE_STUDENT")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void addEnrollment(Long studentId, StudentEnrollmentDto studentEnrollmentDto)
            throws EnrollmentLimitExceededException,NoSuchElementException,HasNoWriteException{

        // Validates the enrollment if its up to 4 enrollments
        if(studentEnrollmentDto.getEnrollments().size() > applicationProperties.getEnrollmentLimitPerStudent()) {
            throw new EnrollmentLimitExceededException("Student Enrollment Exceed");
        }else if (studentEnrollmentDto.getEnrollments().size() < applicationProperties.getEnrollmentLimitPerStudent()){
            throw new EnrollmentLimitExceededException("Student should choose "
                    +applicationProperties.getEnrollmentLimitPerStudent()+" enrollments");
        }




        // Lookup for student
        Optional<Student> optionalStudent = studentRepository.findByStudentId(studentId);
        optionalStudent.orElseThrow(()-> new NoSuchElementException("Student not found"));

        Student student = optionalStudent.get();
        // Entry write Access
        Optional<Entry> entryOptional = entryRepository.findByStudentListEmail(student.getEmail());
        entryOptional.orElseThrow(()-> new NoSuchElementException("Student not found"));

        Entry entry = entryOptional.get();
        if(!entry.getHasWriteAccess()){
            throw new HasNoWriteException("User has no write exception");
        }

        if(!student.getEnrollmentList().isEmpty()) {
            throw new EnrollmentLimitExceededException("Student  has choosen "
                    + applicationProperties.getEnrollmentLimitPerStudent() + " enrollment already");
        }

        List<Enrollment> enrollments = studentEnrollmentDto.getEnrollments().stream()
                .filter(enrolId->{
                    return enrollmentRepository.findById(enrolId).isPresent();
                })
                .map(enrolId->{
                    return enrollmentRepository.findById(enrolId).get();
                }).collect(Collectors.toList());

        if(validateEnrollmentPeriodAndEnrollments(student,enrollments)){
            student.setEnrollmentList(enrollments);
            studentRepository.save(student);
        }


    }

    @Secured({"ROLE_FACULTY","ROLE_STUDENT"})
    @Override
    public List<Enrollment> loadEnrollmentByStudent(Long student) throws NoSuchElementException{
        // Lookup for student
        Optional<Student> optionalStudent = studentRepository.findByStudentId(student);
        optionalStudent.orElseThrow(()-> new NoSuchElementException("Student not found"));

        return optionalStudent.get().getEnrollmentList();
    }


    @Secured({"ROLE_FACULTY","ROLE_STUDENT"})
    @Override
    public Optional<Enrollment> loadStudentEnrollmentByEnrollmentId(Long enrollment, Long studentId) {
        return studentRepository
                .findByEnrollmentListIdAndStudentId(enrollment,studentId)
                .get().getEnrollmentList().stream().filter(enrollment1 -> enrollment1.getId() == enrollment)
                .findFirst();
    }


    @Override
    public void addStudent(Student student) {
        studentRepository.save(student);
    }



    /**
     * Validate Student's enrollment Period
     * @param student
     * @param enrollments
     * @return
     */
    private Boolean validateEnrollmentPeriodAndEnrollments(Student student,List<Enrollment> enrollments){
        Optional<Entry> entryOptional = entryRepository.findByStudentListStudentId(student.getStudentId());
        if(entryOptional.isPresent()){
            // Compare Period
            Entry entry = entryOptional.get();
            for (Enrollment enrollment : enrollments){
                if(enrollment.getEnrolStartDate().compareTo(entry.getEnrolStartDate()) > 0
                        && enrollment.getEnrolEndDate().compareTo(entry.getEnrolStartDate()) < 0){
                    return false;
                }
            }
            return true;
        }
        return false;
    }




}
