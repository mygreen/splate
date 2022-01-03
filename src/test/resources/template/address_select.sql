select * from address
/*BEGIN*/
where
	/*IF pk.employeeId != null*/
	employee_id >= /*pk.employeeId*/'00001'
	/*END*/
	/*IF pk.addressId != null*/
	and address_id <= /*pk.addressId*/1
	/*END*/
	/*IF telNumber != null*/
	and tel_number = /*telNumber*/'090-xxx-xxx'
	/*END*/
/*END*/