select * from employee
/*BEGIN*/
where
/*IF salaryMin != null*/
salary >= /*salaryMin*/1000
/*END*/
/*IF salaryMax != null*/
and salary <= /*salaryMax*/2000
/*END*/
/*END*/