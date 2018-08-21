@isTest
public with sharing class VFC_MonitoringHours_Ext_Test {
    @TestSetup 
    public static void setupData() {
        String orgId = Userinfo.getOrganizationID();        
        String uniqueEmail = 'test@' + orgId + '.net.test';
        User anUser = new User( Alias = 'TST'
                        , CommunityNickname = 'TST'
                        , Email = uniqueEmail
                        , EmailEncodingKey = 'UTF-8'
                        , LastName = 'Testing'
                        , LanguageLocaleKey = 'en_US'
                        , LocaleSidKey = 'en_US'
                        , Profile = new Profile( Name = '#Custom: Sales Management' )
                        , TimezoneSidKey = 'America/Los_Angeles'
                        , UserName = uniqueEmail );
        insert anUser;
        String uniqueEmail2 = 'test2@' + orgId + '.net.test';
        User anUser2 = new User( Alias = 'TST2'
                        , CommunityNickname = 'TST2'
                        , Email = uniqueEmail2
                        , EmailEncodingKey = 'UTF-8'
                        , LastName = 'Testing2'
                        , LanguageLocaleKey = 'en_US'
                        , LocaleSidKey = 'en_US'
                        , Profile = new Profile( Name = '#Custom: Sales Management' )
                        , TimezoneSidKey = 'America/Los_Angeles'
                        , UserName = uniqueEmail2 );
        insert anUser2;

        Account a = new Account();
        a.Name = 'SGI Test One';
        a.Type = 'Customer';
        a.Channel__c = 'Reseller';
        a.Activity_Email__c = 'nopetest@test.com';
        a.RecordType = new RecordType( Name = 'Site' );
        insert a;

        AccountTeamMember m = new AccountTeamMember();
        m.AccountId = a.ID;
        m.TeamMemberRole = 'Account Manager';
        m.UserId = anUser.ID;
        insert m;

        AccountTeamMember m2 = new AccountTeamMember();
        m2.AccountId = a.ID;
        m2.TeamMemberRole = 'Field Services';
        m2.UserId = anUser2.ID;
        insert m2;

        Contact cnt = new Contact();
        cnt.FirstName = 'Test';
        cnt.LastName = 'Contact';
        cnt.AccountID = a.ID;
        insert cnt;

        /*Opportunity o = new Opportunity();
        o.AccountID = a.ID;
        o.Name = 'SGI Test 1';
        o.StageName = '52-installed'; // '40-Contracts Signed';
        o.CloseDate = Date.today();
        o.Contract_Expiration_Date__c = Date.today();
        o.RecordType = new RecordType( Name = 'Fixed' );
        o.Billing_Schedule__c = 'Monthly';
        o.Payment_Terms__c = 'NET 15';
        o.PiQ_Contact__c = cnt.ID;
        insert o;*/

        Product2 p = new Product2();
        p.Name = 'Test Prod';
        p.IsActive = true;
        insert p;

        PricebookEntry pbe = new PricebookEntry();
        pbe.Pricebook2Id = Test.getStandardPricebookId();
        pbe.Product2Id = p.ID;
        pbe.UnitPrice = 102;
        pbe.IsActive = true;
        insert pbe;

        OpportunityLineItem oli = new OpportunityLineItem();
        //oli.OpportunityID = o.ID;
        oli.PricebookEntryID = pbe.ID;
        oli.Quantity = 2;
        oli.TotalPrice = 1020;
        //insert oli;

        Case c = new Case();
        c.AccountID = a.ID;
        c.RecordType = new RecordType( Name = 'Install Request' );
        c.Status = '01-New';
        c.Type = 'Install';
        c.Subject = 'Test Case';
        c.Description = 'Test Description';
        insert c;

        Installation__c i = new Installation__c();
        i.Site__c = a.ID;
        i.Install_Case__c = c.ID;
        i.Guard_Tour_Times__c = 'Sample guard tour hours';
        i.Additional_Monitoring_Hours__c = 'Sample addtl hours';
        i.Monitoring_Hours__c = 'Sample monitoring hours';
        //i.Opportunity__c = o.ID;
        i.Monitoring_Type__c = 'Live Monitoring';
        insert i;
    }

    public static TestMethod void scheduleDescriptionTest() {
        Installation__c i = [ 
                SELECT ID
                    , Monday__c, Tuesday__c, Wednesday__c, Thursday__c, Friday__c
                    , Saturday__c, Sunday__c, X24_Hour_Mon__c, X24_Hour_Tue__c
                    , X24_Hour_Wed__c, X24_Hour_Thu__c, X24_Hour_Fri__c
                    , X24_Hour_Sat__c, X24_Hour_Sun__c, AM_Start_Fri_Hour__c
                    , AM_Start_Fri_Min__c, AM_Start_Mon_Hour__c, AM_Start_Mon_Min__c
                    , AM_Start_Sat_Hour__c, AM_Start_Sat_Min__c, AM_Start_Sun_Hour__c
                    , AM_Start_Sun_Min__c, AM_Start_Thu_Hour__c, AM_Start_Thu_Min__c
                    , AM_Start_Tue_Hour__c, AM_Start_Tue_Min__c, AM_Start_Wed_Hour__c
                    , AM_Start_Wed_Min__c, AM_Stop_Fri_Hour__c, AM_Stop_Fri_Min__c
                    , AM_Stop_Mon_Hour__c, AM_Stop_Mon_Min__c, AM_Stop_Sat_Hour__c
                    , AM_Stop_Sat_Min__c, AM_Stop_Sun_Hour__c, AM_Stop_Sun_Min__c
                    , AM_Stop_Thu_Hour__c, AM_Stop_Thu_Min__c, AM_Stop_Tue_Hour__c
                    , AM_Stop_Tue_Min__c, AM_Stop_Wed_Hour__c, AM_Stop_Wed_Min__c
                    , PM_Start_Fri_Hour__c, PM_Start_Fri_Min__c, PM_Start_Mon_Hour__c
                    , PM_Start_Mon_Min__c, PM_Start_Sat_Hour__c, PM_Start_Sat_Min__c
                    , PM_Start_Sun_Hour__c, PM_Start_Sun_Min__c, PM_Start_Thu_Hour__c
                    , PM_Start_Thu_Min__c, PM_Start_Tue_Hour__c, PM_Start_Tue_Min__c
                    , PM_Start_Wed_Hour__c, PM_Start_Wed_Min__c, PM_Stop_Fri_Hour__c
                    , PM_Stop_Fri_Min__c, PM_Stop_Mon_Hour__c, PM_Stop_Mon_Min__c
                    , PM_Stop_Sat_Hour__c, PM_Stop_Sat_Min__c, PM_Stop_Sun_Hour__c
                    , PM_Stop_Sun_Min__c, PM_Stop_Thu_Hour__c, PM_Stop_Thu_Min__c
                    , PM_Stop_Tue_Hour__c, PM_Stop_Tue_Min__c, PM_Stop_Wed_Hour__c
                    , PM_Stop_Wed_Min__c, Monitoring_Service__c, No_Monitoring__c
                    , Time_Zone__c, Guard_Tour_Interval_Mins__c
                    , Guard_Tour_Duration_Mins__c
                FROM Installation__c
                LIMIT 1 ];

        i.Time_Zone__c = 'CT';
        i.Monday__c = true;
        i.Tuesday__c = true;
        i.Wednesday__c = false;
        i.Thursday__c = false;
        i.Friday__c = false;
        i.Saturday__c = false;
        i.Sunday__c = false;
        i.X24_Hour_Tue__c = true;
        i.Monitoring_Service__c = 'Virtual Guard Tour';
        i.Guard_Tour_Interval_Mins__c = 20;
        i.Guard_Tour_Duration_Mins__c = 10;

        i.AM_Start_Mon_Hour__c = '00';
        i.AM_Stop_Mon_Hour__c = '06';
        i.PM_Start_Mon_Hour__c = '19';
        i.PM_Stop_Mon_Hour__c = '23';
        i.AM_Start_Mon_Min__c = '00';
        i.AM_Stop_Mon_Min__c = '00';
        i.PM_Start_Mon_Min__c = '00';
        i.PM_Stop_Mon_Min__c = '59';

        VFC_MonitoringHours_Ext.setMonitoringDescriptions( i );

        Test.startTest();

        system.assertEquals( '00:00 AM - 06:00 AM and 19:00 PM - 23:59 PM Mon (CT)'
                , i.Monitoring_Hours__c , 'Monitoring Hours test' );
        system.assertEquals( 'Every 20 minutes for 10 minutes'
                , i.Guard_Tour_Times__c, 'Guard Tour Times test' );
        system.assertEquals( '24 hours on Tue (CT)'
                , i.Additional_Monitoring_Hours__c, 'Addtl Mon Hours test' );
        system.assertEquals( 35, Math.round( i.Monitoring_Hours_Every_Week__c )
                , 'Monitoring Hours Every Week test' );

    }

    public static TestMethod void myUnitTest() {
        Installation__c i = [ 
                SELECT ID
                    , Monday__c, Tuesday__c, Wednesday__c, Thursday__c, Friday__c
                    , Saturday__c, Sunday__c, X24_Hour_Mon__c, X24_Hour_Tue__c
                    , X24_Hour_Wed__c, X24_Hour_Thu__c, X24_Hour_Fri__c
                    , X24_Hour_Sat__c, X24_Hour_Sun__c, AM_Start_Fri_Hour__c
                    , AM_Start_Fri_Min__c, AM_Start_Mon_Hour__c, AM_Start_Mon_Min__c
                    , AM_Start_Sat_Hour__c, AM_Start_Sat_Min__c, AM_Start_Sun_Hour__c
                    , AM_Start_Sun_Min__c, AM_Start_Thu_Hour__c, AM_Start_Thu_Min__c
                    , AM_Start_Tue_Hour__c, AM_Start_Tue_Min__c, AM_Start_Wed_Hour__c
                    , AM_Start_Wed_Min__c, AM_Stop_Fri_Hour__c, AM_Stop_Fri_Min__c
                    , AM_Stop_Mon_Hour__c, AM_Stop_Mon_Min__c, AM_Stop_Sat_Hour__c
                    , AM_Stop_Sat_Min__c, AM_Stop_Sun_Hour__c, AM_Stop_Sun_Min__c
                    , AM_Stop_Thu_Hour__c, AM_Stop_Thu_Min__c, AM_Stop_Tue_Hour__c
                    , AM_Stop_Tue_Min__c, AM_Stop_Wed_Hour__c, AM_Stop_Wed_Min__c
                    , PM_Start_Fri_Hour__c, PM_Start_Fri_Min__c, PM_Start_Mon_Hour__c
                    , PM_Start_Mon_Min__c, PM_Start_Sat_Hour__c, PM_Start_Sat_Min__c
                    , PM_Start_Sun_Hour__c, PM_Start_Sun_Min__c, PM_Start_Thu_Hour__c
                    , PM_Start_Thu_Min__c, PM_Start_Tue_Hour__c, PM_Start_Tue_Min__c
                    , PM_Start_Wed_Hour__c, PM_Start_Wed_Min__c, PM_Stop_Fri_Hour__c
                    , PM_Stop_Fri_Min__c, PM_Stop_Mon_Hour__c, PM_Stop_Mon_Min__c
                    , PM_Stop_Sat_Hour__c, PM_Stop_Sat_Min__c, PM_Stop_Sun_Hour__c
                    , PM_Stop_Sun_Min__c, PM_Stop_Thu_Hour__c, PM_Stop_Thu_Min__c
                    , PM_Stop_Tue_Hour__c, PM_Stop_Tue_Min__c, PM_Stop_Wed_Hour__c
                    , PM_Stop_Wed_Min__c, Monitoring_Service__c, No_Monitoring__c
                    , Time_Zone__c, Guard_Tour_Interval_Mins__c
                    , Guard_Tour_Duration_Mins__c
                FROM Installation__c
                LIMIT 1 ];

        Test.startTest();

        // set page and parameters
        Test.setCurrentPage( Page.MonitoringHours );
        ApexPages.CurrentPage().getParameters().put( 'ID', i.ID );

        // instantiate the extension controller
        ApexPages.StandardController stdCtrlr = new ApexPages.StandardController( i );
        VFC_MonitoringHours_Ext theController = new VFC_MonitoringHours_Ext( stdCtrlr );

        theController.i.Monitoring_Service__c = 'Client';
        theController.changedMonitoringService();

        theController.i.Time_Zone__c = 'CT';
        theController.i.Monitoring_Service__c = 'Dedicated';
        theController.changedMonitoringService();

        theController.weekdayList[ 0 ].daySelected = true;
        theController.weekdayList[ 0 ].startAM = '00';
        theController.weekdayList[ 0 ].stopAM = '06';
        theController.weekdayList[ 0 ].startPM = '19';
        theController.weekdayList[ 0 ].stopPM = '23';
        theController.weekdayList[ 0 ].startMinuteAM = '00';
        theController.weekdayList[ 0 ].stopMinuteAM = '00';
        theController.weekdayList[ 0 ].startMinutePM = '00';
        theController.weekdayList[ 0 ].stopMinutePM = '59';
        theController.clickedDay();

        theController.weekdayList[ 1 ].twentyFourHour = true;
        theController.clicked24Hour();

        theController.save();

        Test.stopTest();

        if( ApexPages.getMessages().size() > 0 ) {
            String errorMsg = ApexPages.getMessages()[ 0 ].getSummary();
            system.assert( errorMsg.contains( 'Please enter' ), '1st error: ' + errorMsg );
            system.assert( errorMsg.contains( 'in the opportunity before entering revenue distribution' ), '2nd error: ' + errorMsg );
        }

        i = [ SELECT ID, Additional_Monitoring_Hours__c, Monitoring_Hours__c
                    , Monitoring_Hours_Every_Week__c
                FROM Installation__c
                LIMIT 1 ];

        system.assertEquals( '00:00 AM - 06:00 AM and 19:00 PM - 23:59 PM Mon (CT)'
                , i.Monitoring_Hours__c, 'Monitoring Hours test' );
        system.assertEquals( '24 hours on Tue (CT)'
                , i.Additional_Monitoring_Hours__c, 'Addtl Monitoring Hours test' );
        system.assertEquals( 35
                , Math.round( i.Monitoring_Hours_Every_Week__c ), 'Monitoring Hours Every Week test' );

        // increase test coverage
        List<SelectOption> soList = theController.hourAMList;
        soList = theController.hourPMList;
        soList = theController.minuteList;

        theController.cancel();     
    }

    public static TestMethod void myAccountInstallationTest() {

        Account a = [ SELECT ID 
                    FROM Account 
                    WHERE Name = 'SGI Test One'
                    LIMIT 1 ];

        Test.startTest();

        // set page and parameters
        Test.setCurrentPage( Page.AccountMonitoringHours );
        ApexPages.CurrentPage().getParameters().put( 'ID', a.ID );

        // instantiate the extension controller
        ApexPages.StandardController stdCtrlr = new ApexPages.StandardController( a );
        VFC_MonitoringHours_Ext theController = new VFC_MonitoringHours_Ext( stdCtrlr );

        theController.i.Monitoring_Service__c = 'Client';
        theController.changedMonitoringService();

        theController.i.Time_Zone__c = 'CT';
        theController.i.Monitoring_Service__c = 'Dedicated';
        theController.changedMonitoringService();

        theController.weekdayList[ 0 ].daySelected = true;
        theController.weekdayList[ 0 ].startAM = '00';
        theController.weekdayList[ 0 ].stopAM = '06';
        theController.weekdayList[ 0 ].startPM = '19';
        theController.weekdayList[ 0 ].stopPM = '23';
        theController.weekdayList[ 0 ].startMinuteAM = '00';
        theController.weekdayList[ 0 ].stopMinuteAM = '00';
        theController.weekdayList[ 0 ].startMinutePM = '00';
        theController.weekdayList[ 0 ].stopMinutePM = '59';
        theController.clickedDay();

        theController.weekdayList[ 1 ].twentyFourHour = true;
        theController.clicked24Hour();

        theController.resetSelection();
        theController.syncSelection();

        theController.installSelectionMap.put( theController.i.ID, true );

        theController.save();

        Test.stopTest();

        if( ApexPages.getMessages().size() > 0 ) {
            String errorMsg = ApexPages.getMessages()[ 0 ].getSummary();
            system.assert( errorMsg.contains( 'Please enter' ), '1st error: ' + errorMsg );
            system.assert( errorMsg.contains( 'in the opportunity before entering revenue distribution' ), '2nd error: ' + errorMsg );
        }

        Installation__c i = [ SELECT ID, Additional_Monitoring_Hours__c, Monitoring_Hours__c
                    , Monitoring_Hours_Every_Week__c
                FROM Installation__c
                LIMIT 1 ];

        system.assertEquals( '00:00 AM - 06:00 AM and 19:00 PM - 23:59 PM Mon (CT)'
                , i.Monitoring_Hours__c, 'Monitoring Hours test' );
        system.assertEquals( '24 hours on Tue (CT)'
                , i.Additional_Monitoring_Hours__c, 'Addtl Monitoring Hours test' );
        system.assertEquals( 35
                , Math.round( i.Monitoring_Hours_Every_Week__c ), 'Monitoring Hours Every Week test' );

        // increase test coverage
        List<SelectOption> soList = theController.hourAMList;
        soList = theController.hourPMList;
        soList = theController.minuteList;

        theController.cancel();     
    }

    public static TestMethod void myCaseInstallationTest() {

        Case c = [ SELECT ID 
                    FROM Case 
                    WHERE Subject = 'Test Case'
                    LIMIT 1 ];

        Test.startTest();

        // set page and parameters
        Test.setCurrentPage( Page.CaseMonitoringHours );
        ApexPages.CurrentPage().getParameters().put( 'ID', c.ID );

        // instantiate the extension controller
        ApexPages.StandardController stdCtrlr = new ApexPages.StandardController( c );
        VFC_MonitoringHours_Ext theController = new VFC_MonitoringHours_Ext( stdCtrlr );
    }
}