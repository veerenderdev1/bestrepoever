/**********************************************************************
* Name:     CaseHelper
* Author:   Strategic Growth, Inc. (www.strategicgrowthinc.com)
*  
* ======================================================
* ======================================================
* Purpose:                                                      
* 
* ======================================================
* ======================================================
* History:                                                            
* VERSION   DATE            INITIALS      DESCRIPTION/FEATURES ADDED
* 1.0       24-Feb-2016     rwd           Initial Development           
*   
***********************************************************************/


public class CaseHelper {

    private static Case_Installation_Settings__c settings;

    public static void updateOppsForClosedInstallCases( List<Case> newCases, Map<Id, Case> oldCaseMap )
    {
        /*
            Design requirement 2.6
            Case after update and after insert
            When Case.IsClosed = TRUE AND Status changes to 90-Closed
            Update related Opportunity.Stage = Installed and Opportunity.Install_Date__c = Today()
            Enter line numbers for each Opportunity Product
         */

        System.debug('---> start updateOppsForClosedInstallCases');
        if ( settings == null ) settings = Case_Installation_Settings__c.getInstance();
        if ( Test.IsRunningTest() )
        {
            SG_TestDataGenerator td = new SG_TestDataGenerator();
            settings = td.getCaseInstallationSettings();
        }
        System.debug('---> settings in casehelper: ' + settings);

        // find recently closed installation cases
        // find related opps
        // find opp products for those opps

        //Map<Id, Opportunity> caseIdToOpportunityMap = new Map<Id, Opportunity>();
        Map<Id, List<OpportunityLineItem>> oppIdToOpportunityLineItemMap = new  Map<Id, List<OpportunityLineItem>>();
    //  Map<Id, Case> closedCaseMap = new Map<Id, Case>();
        Map<Id, List<Case>> oppIdToCasesMap = new Map<Id, List<Case>>();

    //  Set<Id> oppIds = new Set<Id>();

        List<Opportunity> updateOpps = new List<Opportunity>();
        List<OpportunityLineItem> updateOliList = new List<OpportunityLineItem>();
        //Map<id,id>  caserecordtype = new Map<id,id>();
        set<id> opportunittyids = new set<id>();
        
        for(case cs: [Select id,Recordtype.name,Opportunity__c  From Case Where ID IN: Newcases])
        {
            if(cs.recordtype.name == 'Install Request' && cs.Opportunity__c != Null)
            {
                opportunittyids.add(cs.Opportunity__c); 
            }
            //caserecordtype.put(cs.id,cs.recordtype.name);
            
        } 
        if ( newCases != null && newCases.size() > 0 )
        {
            for ( Case c : newCases )
            {
                Case oldCase;
                if ( oldCaseMap != null && oldCaseMap.containsKey(c.Id ))
                {
                    oldCase = oldCaseMap.get( c.Id );
                }
                if ( DisplayUtils.hasChanged(oldCase, c, 'Status') && c.Installation_Case_Is_Closed__c )
                {
                    System.debug('---> found closed installation case: ' + c );
                    if ( c.Opportunity__c != null )
                    {
                        List<Case> tmpCases = oppIdToCasesMap.get( c.Opportunity__c );
                        if ( tmpCases == null ) tmpCases = new List<Case>();
                        tmpCases.add( c );
                        oppIdToCasesMap.put( c.Opportunity__c, tmpCases );
                    }

                }
            }

            if ( oppIdToCasesMap != null && oppIdToCasesMap.keySet().size() > 0 )
            {
                // get all the related Opps and update the fields
                System.debug('---> find related opps');
                for ( Opportunity o : [SELECT id, name, amount, stagename, Install_Date__c FROM Opportunity WHERE Id IN :oppIdToCasesMap.keySet() ])
                {
                      
                    
                    System.debug('---> stageName now: ' + o.StageName);
                    if ( o.Install_Date__c == null && opportunittyids.contains(o.id))// if the Cse record type is Install Request
                                                                                     // then it will update Opportunity stage and install date
                    {
                        o.stageName = settings.Opportunity_Closed_Installation_Status__c;
                        o.Install_Date__c = DateTime.now(); //was Date.today();
                        updateOpps.add( o );
                    }


                    
                }

                // get all the related OLIs and set the line number
                System.debug('---> find related opp line items');
                for ( OpportunityLineItem oli : [SELECT id, OpportunityId, Line_Number__c, SortOrder FROM OpportunityLineItem WHERE OpportunityId In :oppIdToCasesMap.keySet() ORDER BY sortOrder, name ])
                {
                    List<OpportunityLineItem> tmpList = oppIdToOpportunityLineItemMap.get( oli.OpportunityId );
                    if ( tmpList == null ) tmpList = new List<OpportunityLineItem>();
                    tmpList.add( oli );
                    oppIdToOpportunityLineItemMap.put( oli.OpportunityId, tmpList );
                }

                if ( oppIdToOpportunityLineItemMap != null && oppIdToOpportunityLineItemMap.keySet().size() > 0 )
                {
                    for ( Id oppId : oppIdToOpportunityLineItemMap.keySet() )
                    {
                        System.debug('---> getting OLIs for oppId: ' + oppId );
                        List<OpportunityLineItem> oliList = oppIdToOpportunityLineItemMap.get( oppId );
                        if ( oliList != null && oliList.size() > 0 )
                        {
                            Integer ctr = 1;
                            for ( OpportunityLineItem oli : oliList )
                            {
                                oli.Line_Number__c = ctr;
                                updateOliList.add( oli );
                                ctr++;
                            }
                        }
                    }
                }

                // update the Opportunities
                System.debug('---> about to update opps: ' + updateOpps);
                if ( updateOpps != null && updateOpps.size() > 0 )
                {
                    update updateOpps;
                }

                // update the OpportunityLineItems
                System.debug('---> about to update OLIs: ' + updateOliList);
                if ( updateOliList != null && updateOliList.size() > 0 )
                {
                    update updateOliList;
                }
            }
        }
    }
    
    public static void notifyToContacts(list<Case> newlist,map<id,Case> oldlist)
    {
        set<id> accountids = new set<id>();
        set<string> emailids = new set<string>();
        string manageremailid ;
        for(Case c : newlist)
        {
            string shippingaddress='';
            system.debug('-------'+c.Status);
            if(c.RecordtypeId == '012o0000000pmZA' && c.Status == '40-Send Representative' && c.Status != oldlist.get(c.id).Status && /*c.Installation__c != null && */c.Install_Type__c == 'Fixed' && c.accountid != null)
            {
                accountids.add(c.AccountId);
            }
            if(!accountids.isEmpty())
            {
                for(Account acc : [Select id,ShippingStreet, ShippingCity, ShippingState, ShippingPostalCode, ShippingCountry, 
                                   ShippingLatitude, ShippingLongitude, Activity_Email__c,
                                   (SELECT id,User.Email FROM AccountTeamMembers WHERE TeamMemberRole = 'Account Manager' LIMIT 1) 
                                   FROM Account WHERE Id IN:accountids])
                {
                    //shippingaddress = string.valueof(acc.ShippingAddress);
                    // shippingaddress = acc.ShippingStreet+', '+acc.ShippingCity+', 
                    //'+acc.ShippingState+', '+acc.ShippingPostalCode+', '+acc.ShippingCountry+', 
                    //'+acc.ShippingLatitude+', '+acc.ShippingLongitude;
                    
                    System.debug('@@@ setAccountToRecipients: a.Activity_Email__c = ' + acc.Activity_Email__c);
                    
                    if(!string.isBlank(acc.ShippingStreet))
                        shippingaddress += acc.ShippingStreet+ '<br>';
                    
                    if(!string.isBlank(acc.ShippingCity ))
                        shippingaddress += acc.ShippingCity+ '<br>';
                    
                    if(!string.isBlank(acc.ShippingState))
                        shippingaddress += acc.ShippingState+ '<br>';
                    
                    if(!string.isBlank(acc.ShippingPostalCode))
                        shippingaddress += acc.ShippingPostalCode+ '<br>';
                    
                    if(!string.isBlank(acc.ShippingCountry))
                        shippingaddress += acc.ShippingCountry+ '<br>';
                    
                    // if(acc.ShippingLatitude!= null)
                    // shippingaddress += acc.ShippingLatitude+ '\n ';
                    
                    //if(acc.ShippingLongitude!= null)
                    //shippingaddress += acc.ShippingLongitude+ '\n ';
                    
                    system.debug('-------'+shippingaddress);
                    
                    if(acc.Activity_Email__c != null)
                    {
                        system.debug('Activity_Email__c:::'+acc.Activity_Email__c);
                        if(acc.Activity_Email__c.contains(','))
                        {
                            String splitToken = ',';
                            List<String> emailAddresses = acc.Activity_Email__c.split(splitToken);
                            if(emailAddresses != null && emailAddresses.size() > 0)
                            {
                                for(Integer i = 0; i < emailAddresses.size(); i++)
                                {   
                                    String email = emailAddresses[i].trim();
                                    if(isValidEmailAddress(email))
                                    {
                                        emailids.add(email);
                                        system.debug('emailids'+emailids);
                                    }
                                }
                            }
                        }
                        
                        if(acc.Activity_Email__c.contains(';'))
                        {
                            String splitToken = ';';
                            List<String> emailAddresses2 = acc.Activity_Email__c.split(splitToken);
                            if(emailAddresses2 != null && emailAddresses2.size() > 0)
                            {
                                for(Integer i = 0; i < emailAddresses2.size(); i++)
                                {   
                                    String email = emailAddresses2[i].trim();
                                    if(isValidEmailAddress(email))
                                    {
                                        emailids.add(email);
                                    }
                                }
                            }
                        }
                        else {
                            if(!acc.Activity_Email__c.contains(';') && !acc.Activity_Email__c.contains(',')){
                                emailids.add(acc.Activity_Email__c);
                                system.debug('Account Activity Email Id:'+emailids);
                            }
                        }
                    }
                    if(acc.AccountTeamMembers != null && acc.AccountTeamMembers.size()>0){
                        system.debug('-------'+acc.AccountTeamMembers[0].User.Email);
                        manageremailid = acc.AccountTeamMembers[0].User.Email;
                        system.debug('Account Manager Email Id:'+manageremailid);
                    }
                }
                if(!emailids.isempty())
                {
                    if(manageremailid== null || manageremailid  == '')
                        emailids.add(system.Label.Default_Email_Address);
                    else
                        emailids.add(manageremailid);
                    list<string> elist = new list<string>();
                    elist.addAll(emailids);
                    system.debug('-------'+elist);
                    EmailTemplate et = [Select  Body, HtmlValue, Id, Name, Subject from EmailTemplate  where id=:system.label.Notification_Template_Id];
                    //OrgWideEmailAddress[] owea = [select Id from OrgWideEmailAddress where Address = 'customer.service@pro-vigil.com'];
                    Messaging.SingleEmailMessage mail = new Messaging.SingleEmailMessage();
                    //if (!test.isRunningTest() &&  owea.size() > 0 ) {
                     //   mail.setOrgWideEmailAddressId(owea.get(0).Id);
                   // }
                    list<Messaging.SingleEmailMessage> theEmails = new list<Messaging.SingleEmailMessage>();
                    //Messaging.SingleEmailMessage mail = new Messaging.SingleEmailMessage();
                    //mail.setOrgWideEmailAddressId(owdlist[0].id);
                    mail.saveAsActivity = false;
                    mail.setSubject(et.Subject);
                    system.debug('-------'+elist);
                    string mailbody = et.HtmlValue;
                    system.debug('-------'+mailbody);
                    
                    mailbody = mailbody.replace('{!Account.ShippingAddress}', shippingaddress);
                    mail.setToAddresses(elist);
                    mail.setHtmlBody(mailbody);
                    theEmails.add(mail);
                    if(theEmails.size()>0)
                    {
                        list<Messaging.Email> allMails = new list<Messaging.Email>();
                        for( Integer j = 0; j < theEmails.size(); j++ ){
                            allMails.add(theEmails.get(j));
                        }
                        if(!test.isRunningTest())
                            Messaging.SendEmailResult[] results = Messaging.sendEmail( allMails );
                    }
                }
            }
        }
    }
    
    public static Boolean isValidEmailAddress(String str)
    {
        System.debug('isValidEmailAddress: str = ' + str); 
        if(str != null && str.trim() != null && str.trim().length() > 0)
        {
            String[] split = str.split('@');
            if(split != null && split.size() == 2)
            {
                split = split[1].split('\\.');
                if(split != null && split.size() >= 2)
                {
                    System.debug('isValidEmailAddress: true');
                    return true;
                }
            }
        }
        System.debug('isValidEmailAddress: false');
        return false;
    }
    
    public static Boolean isNotValidEmailAddress(String str)
    {
        System.debug('*****1*******');
        return !isValidEmailAddress(str);
    }
    
   /*Public Static Void Assigncasetoaccountmanager(List<case> caselist)
    {
         set<id> accountids = new set<id>();
         map<id,id> accountmanagerids = new map<id,id>();
         //map<id,id> caseaccountownerids = new map<id,id>();
         
         for(case cs:caselist)
          {
             
             if(cs.type == 'Quality Assurance' &&  cs.Reason == 'Quality Reasons' &&  cs.Status == '01-New' &&  cs.Accountid != Null)
             {
                   accountids.add(cs.accountid);
                   //caseaccountownerids.put(cs.id,cs.accountid);
                     
             }
         }
         if(!accountids.isempty())
         {
             for(account ac:[select ownerid,owner.managerid from account where id IN:accountids])
             {
                 if(ac.owner.managerid!=null)
                 {
                     accountmanagerids.put(ac.id,ac.owner.managerid);
                 }
             }
         }
         if(!accountmanagerids.isempty())
         {
             for(case cs:caselist)
             {
                 if(cs.type == 'Quality Assurance' &&  cs.Reason == 'Quality Reasons' &&  cs.Status == '01-New' &&  cs.Accountid != Null)  
                 {
                     if(accountmanagerids.containskey(cs.accountid))
                     {
                         cs.ownerid = accountmanagerids.get(cs.accountid);
                     }
                 }               
             }
         }  
   }*/

}