# bestrepoever

2. Deployed changes CHAN-655 and CHAN-612 to production.

What I did not complete?
1. Did not deploy CHAN-629, making final changes to meet the requirement.



Example:Full Name = LastName & ", " & FirstName

https://github.com/veerenderdev1/bestrepoever.git



            if(cs.EmailMessage.status == 'New' || cs.Email_Status__c == 'Email Received'){
                cs.Email_Status__c = 'Email Sent';
                system.debug('First_Response_time__c:'+cs.First_Response_time__c);
            }
			
			Set<id> cid = new Set<id>();
        for(Case cs : Trigger.new){ 
            cid.add(cs.id);
            if(cs.CaseHandledDate__c == null){
                cs.CaseHandledDate__c = system.now();  
            }
            if(cs.CaseHandledDate__c != null){
                cs.CaseHandledDate__c = system.now(); 
            }