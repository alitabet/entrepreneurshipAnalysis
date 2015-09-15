
#delimit ;
set more off;
log close _all;

log using "Where is Silicon Valley.log",replace;


/* Sample Selection Procedure*/

	#delimit ;

	/*Select the correct sample */
	use CA.collapsed.dta,replace;
	

	
	safedrop test train sampletrain _merge;
	merge 1:1 dataid using CA.SampleSelection.dta;
	drop _merge;
	
	/*Label variables for tables*/
	label variable anypatent 		"Patent";
	label variable trademark 		"Trademark";
	label variable eponymous	 	"Eponymous";
	label variable local2			 "Local";
	label variable tech2 			"Technology";
	label variable words_1or2 		"Short Name";
	label variable is_corp 			"Corporation";
	label variable is_DE 			"Delaware Jurisdiction";
	label variable only_DE 			"Only Delaware";
	label variable only_patent 		"Only Patent";
	label variable patent_and_DE 	"Patent and Delaware";
	save CA.collapsed.dta,replace;




/*Build Model and predict stategrowth score*/
	u CA.collapsed.dta,replace;
	safedrop stategrowth;
	
	logit growthz eponymous is_corp local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE 
		  if train & sampletrain 
		  , vce(robust) or;
		  
	predict stategrowth;
	save CA.collapsed.dta,replace;

/*
---------------------------------------------------------------
					Table S1 
---------------------------------------------------------------
*/

	
/* Panel A: Regression Model */
	
	#delimit ;
	eststo clear;
				
	eststo CA_busreg,	title("Business Registration Observables"):
						logit 	growthz  eponymous is_corp local2 tech2 words_1or2 is_DE   
								if train & sampletrain, vce(robust)	or;
								
	eststo CA_external, title("External Observables"):
						logit 	growthz trademark anypatent
								if train & sampletrain , vce(robust)	or;
						
	eststo CA_all, 		title("All"):
						logit 	growthz  eponymous is_corp local2 tech2 words_1or2 trademark only_patent only_DE patent_and_DE 
								if train & sampletrain , vce(robust) or;
	
	
	# delimit ;
	estout CA_busreg  CA_external CA_all  ,  
			replace label cells(b(fmt(a3) star) se(fmt(a2) par([ ]))) 
			postfoot("Robust standard errors in parenthesis.") legend noabbrev  stats(N r2_p, fmt(%9.0g) 
			labels(Observations Pseudo-R2)) 
			order(eponymous words_1or2 local2 tech2 is_corp is_DE trademark anypatent only_patent only_DE patent_and_DE)
			collabels(none) varwidth(25)  eform(1 1 1 1 )  starlevels(+ .05 * .01 ** .001);
		

	# delimit ;
	estout CA_busreg  CA_external CA_all   using "Final Figure 1 Table A - Odds Ratios.txt",  
			replace label cells(b(fmt(a3) star) se(fmt(a2) par([ ]) ))
			postfoot("Robust standard errors in parenthesis.") legend noabbrev  stats(N r2_p, fmt(%9.0g) 
			labels(Observations Pseudo-R2)) varlabels(_const Constant) collabels(none) varwidth(25) 
			order(eponymous words_1or2 local2 tech2 is_corp is_DE trademark anypatent only_patent only_DE patent_and_DE)
			margin eform(1 1 1 1 ) 
			delim(|)  starlevels(+ .05 * 0.01 ** .001);
		

/*
---------------------------------------------------------------
		Panel B: Test of Accuracy of Model
				Using 30% of sample not used
---------------------------------------------------------------
*/
	#delimit ;	
	u CA.collapsed.dta,replace;
	
	/*Keep only sample not used in regression from 2001 to 2006 */
	keep if train &  !sampletrain;
	sort stategrowth;
	keep if _n/_N >= .99;
	collapse (sum) growthz;
	local numtop1 = growthz[1];

	u CA.collapsed.dta,replace;
	keep if train &  !sampletrain; 
	sort stategrowth;
	local N=_N;
	gen percentile=floor(_n/_N*20)/20;
	replace percentile = .95 if percentile == 1;
	collapse (sum) growthz, by(percentile);
	egen totalgrowth = sum(growthz);
	sort percentile;
	local sharetop1 = substr(string(round(`numtop1'/totalgrowth,.01)),1,3);
	local sharetop5 = substr(string(round(growthz[_N]/totalgrowth,.01)),1,3);
	local sharetop10 = substr(string(round((growthz[_N] + growthz[_N-1])/totalgrowth,.01)),1,3);
	
	
	gen percentsuc = growthz[_n]/totalgrowth;
	di "Share top 1%: `sharetop1'";
	di "Share top 5%: `sharetop5'";
	di "Share top 10%: `sharetop10'";
	
	
	graph twoway bar percentsuc percentile, 
			text(.6 .35 "Share of realized growth" "---------------------------" "Top 1%: `sharetop1'"
						 "Top 5%: `sharetop5'" "Top 10%: `sharetop10'", justification(left)) 
			barwidth(.04) xlabel(0(.05).95) ytitle("Percent of realized growth events")
			title("Estimated Entrepreneurial Quality Percentile vs. Incidence" "of Realized Growth Outcomes (30% 2001-2006 Test Sample)", size(medium)) 
			saving("accuracy_test.gph",replace)
			xtitle("Percent Realized Growth Events") ytitle("Percentile");
			

	


/*
---------------------------------------------------
 Build City Dataset
		Merge with Census incorporated cities and population list.
---------------------------------------------------
*/
	# delimit ;
	clear ;
	u CA.collapsed.dta,replace;
	gen obs = 1;
	keep if inrange(incyear,2007,2011);
	replace city = trim(itrim(upper(city)));
	replace city = subinstr(city,",","",.);
	collapse (sum) obs  (mean) stategrowth , by( city) ;
	save svresults.dta,replace;

	u population.dta,replace;
	merge 1:1 city  using svresults.dta;
	drop if _merge != 3;
	drop _merge;
	gsort -stategrowth;
	save svresults.dta,replace;
	
	/* Save the city rankings */
	outsheet using "Figure 3 - City Rankings.csv"
			,comma names noquote replace;

	/* Save percentile distribution */
	sort stategrowth;
	gen percentile = floor(_n/_N*100);
	replace percentile = 99 if percentile == 100;
	collapse (min) start_interval = stategrowth (max) end_interval=stategrowth, by(percentile);
	
	
	
/* Figure S1: Quality vs Quantity Comparison */
	
	# delimit ;
	u svresults.dta,replace;
	gen rqual = stategrowth*1000;
	gen rquant = obs/pop*1000;
	drop if missing(rquant);

	regress rqual rquant,robust;
	predict rqualhat;
	
	matrix b1=e(b);
	matrix V1=e(V);
	local rbeta=round(b1[1,1],.00001);
	local rse=round(sqrt(V1[1,1]),.00001);
	local rR2=round(`e(r2)',.00001);
	local rN = `e(N)';

	gsort -rquant;
	safedrop outliers;
	gen outliers = rquant >= 400;
	gen outside = 399;
	replace rquant = round(rquant,1) if outliers;
	
	
	locpoly rqual rquant if !outliers 
			, adoonly plot(line rqualhat rquant if !outliers, lpattern(dash) || 
						 scatter rqual outside if outliers
						, msymbol(Oh) mlabel(rquant) mlabposition(12)) 
			xtitle("Entrepreneurial Quantity (Firms Births per 1000 Residents)") title("") subtitle("") 
			width(10) xlabel(0(50)130) ytitle("Entrepreneurial Quality") msymbol(Oh)   
			title("Estimated Entrepreneurial Quality vs Estimated Entrepreneurial Quantity") legend(label(1 "All California Cities") 
			label(2 "Nonparametric Local-Linear Regression") label(3 "Linear Regression") 
				  cols(1) order(1 2 3)) 
	
			text(2 150 "Regression Coef" "{&beta}=`rbeta'" "se=`rse'" "R{superscript:2}=`rR2'" "N=`rN'"
			     , justification(left) 
		     size(small));
	

/*
--------------------------------------------------------------------
			Figure S3
			
--------------------------------------------------------------------
*/
	# delimit ;
	clear;
	u CA.collapsed.dta;
	
	/* Only look at firms with predicted scores outside the training data */
	keep if test;
	merge 1:m dataid using elcaminoreal.dta;
	keep if _merge==3;
	drop _merge;
	replace address = subinstr(address,",","",.);
	replace entityname  = subinstr(entityname ,",","",.);
	order    address entityname stategrowth;
	
	/* Use only the firms in the block of el Camino Real that we present */
	keep address entityname stategrowth anypatent  trademark eponymous local2 tech2 words_1or2 is_corp is_DE ;
	order address entityname stategrowth anypatent trademark eponymous local2 tech2 words_1or2 is_corp is_DE ;
	
	split address, limit(2);
	drop address2;
	rename address1 addressnumber ;
	gsort addressnumber -stategrowth;
	outsheet using elcaminoreal.csv,comma names noquote replace;
	
	/* Store the distribution of all firm level scores */
	clear;
	u CA.collapsed.dta;
	keep if test;
	sort stategrowth;
	gen percentile = floor(_n/_N*100);
	replace percentile = 99 if percentile == 100;
	collapse (min) start_interval = stategrowth (max) end_interval=stategrowth, by(percentile);
	
	
/*
--------------------------------------------------------------------

				Figure 1
--------------------------------------------------------------------
*/
	
	clear;
	u svresults.dta;
	gen quality = stategrowth*1000;
	

/*		
--------------------------------------------------------------------
				Figure 2
--------------------------------------------------------------------
*/
	
		
	clear;
	u zips.CA.llc.dta;
	append using zips.CA.corp.dta;

	/*trim 9-digit zipcodes */
	replace zipcode = substr(zipcode,1,5) if length(zipcode) > 5;
	drop if length(zipcode) < 5;
	replace city = trim(itrim(upper(city)));

	merge 1:1 dataid city using CA.collapsed.dta;
	keep if inrange(incyear,2007,2011) &  _merge == 3;
	duplicates drop dataid zipcode, force;
	gen obs = 1;
	collapse  (sum) obs (mean) stategrowth, by(zipcode);
	save zipcodes.dta,replace;
	outsheet using zipcodes.csv, comma names noquote replace;
	
	
	
/*Close log */
log close;

	









