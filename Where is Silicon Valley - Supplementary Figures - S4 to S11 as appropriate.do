
	clear
	u CA.collapsed.dta
	local regressors anypatent trademark eponymous local2 tech2 words_1or2 is_corp is_DE
** Table S5: Summary Stats Training Period
	
	by growth,sort: fsum  eponymous is_corp local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE  if train , stats(N mean sd) uselabel format(%9.5f)
	 fsum  eponymous is_corp local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE  if train , stats(N mean sd) uselabel format(%9.5f)


** Table S6: Summary Statistics of Regressors
	clear
	u CA.collapsed.dta	
	by test sampletrain,sort: fsum  eponymous is_corp local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE  growthz, stats(N mean sd) uselabel format(%9.5f)

	
	
	** T-test between train and test. Use testorder variable so that sign of t-stat matches figure reporting
	gen testorder = !sampletrain
	
	di "   ---- T-Test for each variable for train v test samples in 2001-2006 ---- "
	foreach v of varlist `regressors'   growthz{
		
		qui: ttest `v' if train, by(testorder)
		di "`v'    `r(t)'"
	}
	
	
	
	** T-test between model and predict
	drop testorder
	gen testorder = !train
	di "   ---- T-Test for each variable for predict sample 2007-2011 v  2001-2006 ---- "
	foreach v of varlist eponymous is_corp local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE   {
		qui: ttest `v', by(testorder)
		di "`v'    `r(t)'"
	}
	

	
	
** Table S7: Regression
	clear
	di " --- Table S4: Regression --- " 
	u CA.collapsed.dta
	eststo clear
	eststo CA_extrinsic, title("Business Registration Observables"):logit growth eponymous is_corp local2 tech2 words_1or2   if train & sampletrain , vce(robust)	or
	eststo CA_intrinsic, title("External Observables"):logit growth  trademark anypatent if train & sampletrain , vce(robust) or		
	eststo CA_both, title("Combined"):logit growth eponymous is_corp local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE  if train & sampletrain , vce(robust) or
	estout CA_intrinsic CA_extrinsic  CA_both CA_both_dropma CA_both_growth7  ,  replace label cells(b(fmt(a3) star) se(fmt(a2) par([ ]))) postfoot("Robust standard errors in parenthesis.") legend noabbrev  stats(N r2_p, fmt(%9.0g) labels(Observations Pseudo-R2)) collabels(none) varwidth(25)  starlevels(+ .05 * .01 ** .001)	
	*Store it in a file
	estout  CA_intrinsic CA_extrinsic  CA_both CA_both_dropma CA_both_growth7 using "Supplementary Materials - Table S4 - Regression Coefficients.txt",  replace label cells(b(fmt(a3) star) se(fmt(a2) par([ ]))) postfoot("Robust standard errors in parenthesis.") legend noabbrev  stats(N r2_p, fmt(%9.0g) labels(Observations Pseudo-R2)) collabels(none) varwidth(25) starlevels(+ .05 * .01 ** .001)
	
	
** Table S8: Robustness Test

	# delimit ;
	eststo clear;
	
	eststo CA_corp, 		title("Corporations Only"):
						logit 	growthz  eponymous  local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE 
								if train & sampletrain & is_corp , vce(robust) or;
	eststo CA_non_local, 		title("Traded Only"):
						logit 	growthz  eponymous is_corp  tech2 words_1or2 trademark only_patent only_DE patent_and_DE 
								if train & sampletrain & !local2 , vce(robust) or;
	
	# delimit cr						
	estout  , eform  replace label cells(b(fmt(a3) star) se(fmt(a2) par([ ]))) postfoot("Robust standard errors in parenthesis.") legend noabbrev  stats(N r2_p, fmt(%9.0g) labels(Observations Pseudo-R2)) collabels(none) varwidth(25)  starlevels(+ .05 * .01 ** .001)	
	estout   using "Supplementary Materials - Table S5 - Robustness Tests.txt", eform replace label cells(b(fmt(a3) star) se(fmt(a2) par([ ]))) postfoot("Robust standard errors in parenthesis.") legend noabbrev  stats(N r2_p, fmt(%9.0g) labels(Observations Pseudo-R2)) collabels(none) varwidth(25) starlevels(+ .05 * .01 ** .001)
	
	
** Table S9: Summary stats of index
	clear
	u CA.collapsed.dta
	keep if test
	sum stategrowth, detail
	sort stategrowth
	gen bottom99 = _n/_N < .99
	kdensity stategrowth if bottom99, bwidth(.001) title("Density of P(growth) at firm level") subtitle("2007-2011 sample, excluding top 1% of firms") 
	
	sum stategrowth if !bottom99,detail
	
	clear 
	u zipcodes.dta
	replace stategrowth = stategrowth*1000
	sum stategrowth, detail
	sort stategrowth
	gen bottom99 = _n/_N < .99
	label variable stategrowth "E[P(growth)] X 1000"
	kdensity stategrowth if bottom99, bwidth(1) title("Density of P(growth) at zip code level") subtitle("2007-2011 sample, excluding top 1% of zip codes") 
	sum stategrowth if !bottom99,detail
	
	clear 
	u svresults.dta
	replace stategrowth = stategrowth*1000
	sum stategrowth, detail
	sort stategrowth
	gen bottom99 = _n/_N < .99
	label variable stategrowth "E[P(growth)] X 1000"
	kdensity stategrowth if bottom99, bwidth(1) title("Density of P(growth) at city level") subtitle("2007-2011 sample, excluding top 1% of cities") 
	sum stategrowth if !bottom99,detail
