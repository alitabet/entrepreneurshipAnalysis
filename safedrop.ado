**
*
* Summary:
*   Drops variables if they exist, does nothing if not 
*
*

capture confirm program drop safedrop

program define safedrop, rclass
	syntax namelist, [onlyexisting]
	foreach v in `namelist' {
		
		capture confirm variable `v' 
		
		if _rc == 0 {
			drop `v'
			display "Dropping variable `v'", as text
		}
		else{
			if "`onlyexisting'" == ""{
				display "Variable `v' not dropped since it does not exist", as text
			}
		}
	}
end
