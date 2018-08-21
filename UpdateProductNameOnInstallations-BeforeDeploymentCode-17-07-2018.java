public class UpdateProductNameOnInstallations {
    
    public static void UpdateProductName(List<Installation__c> newList){
        
        Set<Id> insIds = new Set<Id>();
        
        //Fetch the Installation__c Id
        for(Installation__c ins : newList) {
            insIds.add(ins.id);
        }   
        
        //Retrieve the Installation and related Opportunity records
        List<Installation__c> insList = [SELECT Id, Name, Product_Name__c, Opportunity__r.id from
                                         Installation__c where id =: insIds]; 
        Set<Id> opId = new Set<Id>();
        //Fetch the Opportunity Id from the Installation List
        for(Installation__c ins2 : insList) {
            opId.add(ins2.Opportunity__r.id);
        } 
        
        ////Fetch all OpportunityLineItems related to the Opportunity which is related to Installation__c
        List<OpportunityLineItem> oliList = [Select Id, Product2.Name From OpportunityLineItem where OpportunityId =: opId]; 
        
        //Loop through all records to find out the product name contains 'Cloud Storage' 
        for(Installation__c i: newList){
            for(OpportunityLineItem li: oliList){
                if(li.Product2.Name.Contains('Cloud Storage')){
                    if(i.Product_Name__c == Null || i.Product_Name__c == ''){
                        i.Product_Name__c = li.product2.Name;                      
                    }
                }
            }
        }
    }
    
    public static void removeProductName(List<OpportunityLineItem> newList){
        
        Set<Id> allOppIds = new Set<Id>();
        //Get the OpportunityID related to the OpportunityLineItems
        system.debug('****'+newList);
        for(OpportunityLineItem oli : newList) {
            allOppIds.add(oli.OpportunityId);
        }         
        List<Installation__c> updateins = new list<Installation__c>();
        //Retrieve all the OpportunityLineItems and Installation records related to the Opportunity
        List<Opportunity> oppList = [SELECT Id, Name, (SELECT Id, Product2.name
                                                       FROM OpportunityLineItems), (select id, Product_Name__c from Installations__r) 
                                     FROM Opportunity WHERE Id=:allOppIds];          
        //Loop through all records to find out the product name contains 'Cloud Storage'
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
    }
}