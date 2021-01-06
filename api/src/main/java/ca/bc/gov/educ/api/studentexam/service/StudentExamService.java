package ca.bc.gov.educ.api.studentexam.service;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.educ.api.studentexam.model.dto.Course;
import ca.bc.gov.educ.api.studentexam.model.dto.StudentExam;
import ca.bc.gov.educ.api.studentexam.model.entity.StudentExamEntity;
import ca.bc.gov.educ.api.studentexam.model.transformer.StudentExamTransformer;
import ca.bc.gov.educ.api.studentexam.repository.StudentExamRepository;
import ca.bc.gov.educ.api.studentexam.util.StudentExamApiConstants;
import ca.bc.gov.educ.api.studentexam.util.StudentExamApiUtils;

@Service
public class StudentExamService {

    @Autowired
    private StudentExamRepository studentExamRepo;

    Iterable<StudentExamEntity> studentExamEntities;

    @Autowired
    private StudentExamTransformer studentExamTransformer;
    
    @Value(StudentExamApiConstants.ENDPOINT_COURSE_BY_CRSE_CODE_URL)
    private String getCourseByCrseCodeURL;
    
    @Autowired
    RestTemplate restTemplate;

    private static Logger logger = LoggerFactory.getLogger(StudentExamService.class);

     /**
     * Get all student exams by PEN populated in Student Exam DTO
     * @param accessToken 
     *
     * @return Student Exam 
     * @throws java.lang.Exception
     */
    public List<StudentExam> getStudentExamList(String pen, String accessToken) {
        List<StudentExam> studentExam  = new ArrayList<StudentExam>();
        HttpHeaders httpHeaders = StudentExamApiUtils.getHeaders(accessToken);
        try {
        	studentExam = studentExamTransformer.transformToDTO(studentExamRepo.findByPen(pen));
        	studentExam.forEach(sE -> {
        		Course course = restTemplate.exchange(String.format(getCourseByCrseCodeURL,sE.getCourseCode(),sE.getCourseLevel()), HttpMethod.GET,
        				new HttpEntity<>(httpHeaders), Course.class).getBody();
        		if(course != null) {
        			sE.setCourseName(course.getCourseName());
        		}
        	});
            logger.debug(studentExam.toString());
        } catch (Exception e) {
            logger.debug("Exception:" + e);
        }

        return studentExam;
    }
}
