@isTest
public class UpdateProductNameOnInstallationsTest {
    @isTest
    public static void UpdateProductNameTestMethod(){
        
        List<Installation__c> newList = new List<Installation__c>();
        
        //Create Test Account
        Account a = new Account();
        a.Name = 'Test';
        insert a;
        
        Date closeDt = Date.Today();
        
        //Create Test Case
        Case cas = new Case(Status ='New', Priority = 'Medium', Origin = 'Email'); 
        insert cas;
        
        //Fetch Standard Price Book ID
        Id pricebookId = Test.getStandardPricebookId();
        
        //Create Test Products
        Product2 prd1 = new Product2 (Name='Fixed - Cloud Storage per Camera (15 Days)', Description='Test Product Entry 1', productCode = 'ABC', isActive = true);
        insert prd1;
        
        Product2 prd2 = new Product2 (Name='Test Product Entry 2', Description='Test Product Entry 2', productCode = 'ABD', isActive = true);
        insert prd2;
        
        //Create Test PricebookEntrys
        PricebookEntry pbe1 = new PricebookEntry (Product2ID=prd1.id, Pricebook2ID=pricebookId,UnitPrice=50, isActive=true);
        insert pbe1;
        
        PricebookEntry pbe2 = new PricebookEntry (Product2ID=prd2.id, Pricebook2ID=pricebookId,UnitPrice=60, isActive=true);
        insert pbe2;
        
        //Create Test opportunity
        opportunity oppr = new opportunity(Name='Test Opportunity' , Pricebook2Id = pbe1.Pricebook2Id, AccountId=a.Id, StageName = 'Prospecting', CloseDate = closeDt);
        insert oppr;
        
        //Create Test installation
        Installation__c installation = new Installation__c(Name = 'Test Installation', Product_Name__c = '', Opportunity__c = oppr.Id, Site__c = a.id, Install_Case__c = cas.id);
        insert installation;
        
        //Create Test OpportunityLineItems
        OpportunityLineItem oppLine1 = new OpportunityLineItem(pricebookentryid = pbe1.Id, 
                                                               TotalPrice = 2000, Quantity = 2, OpportunityID = oppr.Id);
        insert oppLine1;
        
        OpportunityLineItem oppLine2 = new OpportunityLineItem(pricebookentryid = pbe2.Id, 
                                                               TotalPrice = 3000, Quantity = 3, OpportunityID = oppr.Id);
        insert oppLine2;
        
        Test.startTest();
        //Update Installatiosn Name
        installation.Name = 'Test Installation2';
        update installation;
        
        Set<Id> insIds = new Set<Id>();
        
        //Fetch Installatiosn Id
        for(Installation__c ins : newList) {
            insIds.add(ins.id);
        }
        
        //Fetch Installatiosn record and related Opportunity Id
        List<Installation__c> insList = [SELECT Id, Name, Product_Name__c, Opportunity__r.id from
                                         Installation__c where id =: insIds];
        
        Set<Id> opId = new Set<Id>();
        
        //Fetch Opportunity Id from the List
        for(Installation__c ins2 : insList) {
            opId.add(ins2.Opportunity__r.id);
        }
        
        //Fetch OpportunityLineItems related to the Opportunity which is related to Installations
        List<OpportunityLineItem> oliList = [Select Id, Product2.Name From OpportunityLineItem where OpportunityId =: opId]; 
        
        //Loop through all records to find out the product name contains 'Cloud Storage' and Update if name contains.
        for(Installation__c insta: newList){
            for(OpportunityLineItem li: oliList){
                if(li.Product2.Name.Contains('Cloud Storage')){
                    if(insta.Product_Name__c == Null || insta.Product_Name__c == ''){
                        insta.Product_Name__c = li.product2.Name;                      
                    }
                }
                if(!li.Product2.Name.Contains('Cloud Storage')){
                    if(insta.Product_Name__c != Null){
                        insta.Product_Name__c = '';
                    }
                } 
            }
        }
        Test.stopTest();
    }
    
    @isTest
    public static void removeProductNameTestMethod(){
        
        List<OpportunityLineItem> newList = new List<OpportunityLineItem>();
        
        //Create Test Account
        Account a = new Account();
        a.Name = 'Test';
        insert a;
        
        Date closeDt = Date.Today();
        
        //Create Test Case
        Case cas = new Case(Status ='New', Priority = 'Medium', Origin = 'Email'); 
        insert cas;
        
        //Fetch Standard Price Book ID
        Id pricebookId = Test.getStandardPricebookId();
        
        //Create Test Products
        Product2 prd1 = new Product2 (Name='Fixed - Cloud Storage per Camera (15 Days)', Description='Test Product Entry 1', productCode = 'ABC', isActive = true);
        insert prd1;
        
        Product2 prd2 = new Product2 (Name='Test Product Entry 2', Description='Test Product Entry 2', productCode = 'ABD', isActive = true);
        insert prd2;
        
        //Create Test PricebookEntrys
        PricebookEntry pbe1 = new PricebookEntry (Product2ID=prd1.id, Pricebook2ID=pricebookId,UnitPrice=50, isActive=true);
        insert pbe1;
        
        PricebookEntry pbe2 = new PricebookEntry (Product2ID=prd2.id, Pricebook2ID=pricebookId,UnitPrice=60, isActive=true);
        insert pbe2;
        
        //Create Test opportunity
        opportunity oppr = new opportunity(Name='Test Opportunity' , Pricebook2Id = pbe1.Pricebook2Id, AccountId=a.Id, StageName = 'Prospecting', CloseDate = closeDt);
        insert oppr;
        
        //Create Test installation
        Installation__c installation = new Installation__c(Name = 'Test Installation', Product_Name__c = 'Test Product', Opportunity__c = oppr.Id, Site__c = a.id, Install_Case__c = cas.id);
        insert installation;
        
        //Create Test OpportunityLineItems
        OpportunityLineItem oppLine1 = new OpportunityLineItem(pricebookentryid = pbe1.Id, 
                                                               TotalPrice = 2000, Quantity = 2, OpportunityID = oppr.Id);
        insert oppLine1;
        
        OpportunityLineItem oppLine2 = new OpportunityLineItem(pricebookentryid = pbe2.Id, 
                                                               TotalPrice = 3000, Quantity = 3, OpportunityID = oppr.Id);
        insert oppLine2;
        
        //Delete OpportunityLineItem to call the Trigger
        delete oppLine1;
        
        Test.startTest();
        Set<Id> allOppIds = new Set<Id>();
        
        //Fetch Opportunity Id
        for(OpportunityLineItem oli : newList) {
            allOppIds.add(oli.OpportunityId);
        } 
        List<Installation__c> updateins = new list<Installation__c>();
        //Fetch Installatiosn record and related Opportunity Id
        List<Opportunity> oppList = [SELECT Id, Name, (SELECT Id, Product2.name
                                                       FROM OpportunityLineItems), (select id, Product_Name__c from Installations__r) 
                                     FROM Opportunity WHERE Id=:allOppIds];         
        //Loop through all records to find out the product name does not contains 'Cloud Storage' and Update  installation__c
        for(Opportunity o: oppList){
            for(OpportunityLineItem li: o.OpportunityLineItems){
                if(!li.Product2.Name.Contains('Cloud Storage')){
                    system.debug('******'+li.product2.Name);
                    for(Installation__c insta: o.Installations__r){
                        system.debug('******'+insta.Product_Name__c);
                        if(insta.Product_Name__c != Null){
                            insta.Product_Name__c = '';
                            updateins.add(insta);
                        }
                    }
                }
                break;
            }
        }
        //Update Installation Product
        update updateins;
        Test.stopTest();
    }
}