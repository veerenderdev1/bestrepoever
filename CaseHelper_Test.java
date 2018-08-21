/**********************************************************************
* Name:     CaseHelper_Test
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
* 1.0       23-Mar-2016     rwd           Initial Development           
*   
***********************************************************************/


@IsTest
private class CaseHelper_Test {


    private static SG_TestDataGenerator td;
    private static Case_Installation_Settings__c settings;

    @testSetup static void setupTestData() {
        System.debug('---> start CaseHelper_Test setup data');
        // create Accounts, Opps, Opp Products, Cases
        if ( td == null ) td = new SG_TestDataGenerator();

        td.accountNum = 1;
        td.opptyNum = 1;

        List<Account> accts = td.getAccounts(true);
        List<Product2> prods = td.getProducts(true);

        OpportunityValidator.skipValidation = true;

        List<Opportunity> opps = td.getOpportunities(true);
        List<OpportunityLineItem> lineItems = td.getOpportunityLineItems(true);
        List<Case> cases = td.getCases(true);
        settings = td.getCaseInstallationSettings();

        List<AccountTeamMember> members = new List<AccountTeamMember>();
        
        List<Id> oppIds = new List<Id>();
        for ( Opportunity o : opps )
        {
            oppIds.add( o.Id );
        }

        opps = [SELECT id, name, accountId, Validate_Account_Team_Member_Role__c, StageName FROM Opportunity WHERE Id IN :oppIds];

        for ( Opportunity o : opps )
        {
            System.debug('---> setup opp: ' + o);
            AccountTeamMember member = new AccountTeamMember();
            member.AccountId = o.AccountId;
            member.UserId = UserInfo.getUserId();
            member.TeamMemberRole = 'Account Manager';
            members.add( member );

            AccountTeamMember member2 = new AccountTeamMember();
            member2.AccountId = o.AccountId;
            member2.UserId = UserInfo.getUserId();
            member2.TeamMemberRole = 'Field Services';
            members.add( member2 );
        }

        System.debug('---> inserting setup account team members: ' + members);
        insert members;
    }

    static testMethod void testBehavior() {
        System.debug('---> start testBehavior (CaseHelper_Test)');

        /*
            Design requirement 2.6
            Case after update and after insert
            When Case.IsClosed = TRUE AND Status changes to 90-Closed
            Update related Opportunity.Stage = Installed and Opportunity.Install_Date__c = Today()
            Enter line numbers for each Opportunity Product
         */

        if ( td == null ) td = new SG_TestDataGenerator();
        if ( settings == null ) settings = td.getCaseInstallationSettings();

        td.accountNum = 1;
        td.opptyNum = 1;
        OpportunityValidator.skipValidation = true;

        System.debug('---> settings: ' + settings);
        System.debug('---> Opportunity_Closed_Installation_Status__c: ' + settings.Opportunity_Closed_Installation_Status__c);

        List<CaseStatus> closedCaseStatuses = [Select Id, MasterLabel From CaseStatus Where IsClosed = true];
        String closedStatus = closedCaseStatuses[0].MasterLabel;

        List<Case> cases = [SELECT id, status, IsClosed, Installation_Case_Is_Closed__c FROM Case];
        List<Opportunity> opps = [SELECT Id, StageName, Install_Date__c, AccountId FROM Opportunity];

        List<Id> oppIds = new List<Id>();
        Set<Id> acctIds = new Set<Id>();
        string accid;
        for ( Opportunity o : opps )
        {
            System.debug('---> in caseHelper test: ' + o);
            oppIds.add( o.Id );
            acctIds.add( o.AccountId );
            accid = o.AccountId ;
        }

        System.debug('---> acctIds: ' + acctIds);

        List<AccountTeamMember> teamMembers = [SELECT id, AccountId, UserId, TeamMemberRole FROM AccountTeamMember WHERE AccountId IN :acctIds];

        System.debug('---> teamMebers: ' + teamMembers);

        Case closeCase = cases[0];
        System.debug('---> closeCase before update: ' + closeCase);

        closeCase.Opportunity__c = opps[0].Id;
        closeCase.status = closedStatus;
        System.debug('---> closeCase: ' + closeCase);

        OpportunityValidator.skipValidation = true;
        update cases;
        /*Unit__c u = new Unit__c(Name='test');
        insert u;
        Contact con = new COntact(Firstname='test',Lastname='test',Email='test@gmail.com',Accountid=accid);
        insert con;
        Case c = new Case(accountid=accid,type='Account Change',status='01-New',subject='test',Description='test');
        insert c;
        Installation__c ins = new Installation__c(Site__c=accid,Status__c='2-Installed',Unit__c=u.id,
                                Installed_Date__c=Date.today() ,
                                Air_Card_Hex__c='Test' ,
                                Facility__c ='Pro-Vigil',
                                Carrier__c ='AT&T',
                                Static_IP__c='0.0.0.0' ,
                                Power_Meter__c='Yes',
                                IP_Power_Box__c= 'No',
                                Power_Source__c= '220V - Main',
                                Monitoring_Service__c ='Client',
                                Time_Zone__c= 'CT',
                                I_Vigil_Test__c='Yes' ,
                                Cameras_to_Monitor__c= 'All',
                                I_Vigil_Site_Description__c='Yes' ,
                                i_Vigil_Boundaries__c ='Yes',
                                Residential_Area__c ='Yes',
                                Sales_Rep_Present__c ='Yes',
                                Install_Duration__c ='Under an hour',
                                Pro_Vigil_Signage__c='Yes' ,
                                Pigtail_Fixture__c='Yes' ,
                                Electrical_Cable_Length__c='25' ,
                                Average_upload_speed_Mbps__c =2,
                                Install_Case__c=c.id
                                
                                );
        insert ins;*/
        
        //Notify Contacts..
        //Case Case1 = cases[0];
        closeCase.RecordtypeId = '012o0000000pmZA';
        closeCase.Status = '40-Send Representative';
        //closeCase.Installation__c = ins.id;
        closeCase.Install_Type__c = 'Fixed';
        update closeCase;
        
        /*
        opps = [SELECT Id, StageName, Install_Date__c FROM Opportunity];
        for ( Opportunity o : opps )
        {
            if ( o.Id == closeCase.Opportunity__c )
            {
                System.debug('---> o.Install_Date__c: ' + o.Install_Date__c);
                System.debug('---> todays date: ' + Date.today() );
                String todayString = Date.today().day() + '/' + Date.today().month() + '/' + Date.today().year();
                //String installString = o.Install_Date__c.day() + '/' + o.Install_Date__c.month() + '/' + o.Install_Date__c.year();

                //System.assertEquals( todayString, installString);
            }
        }*/

    }
    static testMethod void testBehavior1() {
        System.debug('---> start testBehavior (CaseHelper_Test)');

        /*
            Design requirement 2.6
            Case after update and after insert
            When Case.IsClosed = TRUE AND Status changes to 90-Closed
            Update related Opportunity.Stage = Installed and Opportunity.Install_Date__c = Today()
            Enter line numbers for each Opportunity Product
         */

        if ( td == null ) td = new SG_TestDataGenerator();
        if ( settings == null ) settings = td.getCaseInstallationSettings();

        td.accountNum = 1;
        td.opptyNum = 1;
        OpportunityValidator.skipValidation = true;

        System.debug('---> settings: ' + settings);
        System.debug('---> Opportunity_Closed_Installation_Status__c: ' + settings.Opportunity_Closed_Installation_Status__c);

        List<CaseStatus> closedCaseStatuses = [Select Id, MasterLabel From CaseStatus Where IsClosed = true];
        String closedStatus = closedCaseStatuses[0].MasterLabel;

        List<Case> cases = [SELECT id, status, IsClosed, Installation_Case_Is_Closed__c FROM Case];
        List<Opportunity> opps = [SELECT Id, StageName, Install_Date__c, AccountId FROM Opportunity];

        List<Id> oppIds = new List<Id>();
        Set<Id> acctIds = new Set<Id>();
        string accid;
        for ( Opportunity o : opps )
        {
            System.debug('---> in caseHelper test: ' + o);
            oppIds.add( o.Id );
            acctIds.add( o.AccountId );
            accid = o.AccountId ;
        }

        System.debug('---> acctIds: ' + acctIds);

        List<AccountTeamMember> teamMembers = [SELECT id, AccountId, UserId, TeamMemberRole FROM AccountTeamMember WHERE AccountId IN :acctIds];

        System.debug('---> teamMebers: ' + teamMembers);

        Case closeCase = cases[0];
        
        //Notify Contacts..
        closeCase.RecordtypeId = '012o0000000pmZA';
        closeCase.Status = '40-Send Representative';
        //closeCase.Installation__c = ins.id;
        closeCase.Install_Type__c = 'Fixed';
        update closeCase;
        
       

    }

}