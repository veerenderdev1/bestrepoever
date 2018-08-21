public class OpportunityUpdater {
    public static void updateOpp(List<Opportunity> newList){
        
        System.debug('***********************************************');
        
        Set<Id> oppId = new Set<Id>();
        
        for(Opportunity op: newList){
            oppId.add(op.id);
        } 
        
        List<Opportunity> oppList = [SELECT Id, Name, Account.Carefree_Maintenance_and_Support_Opt_in__c, Account.Monthly_Holiday_Protection_Plan_Opt_in__c,
                                     Carefree_Maintenance_and_Support_Opt_in__c, Monthly_Holiday_Protection_Plan_Opt_in__c,
                                     (SELECT Id, Product2.name FROM OpportunityLineItems) 
                                     FROM Opportunity WHERE Id=:oppId]; 
        
        List<Opportunity> UpdateOpp = new List<Opportunity>();
        List<Account> UpdateAcc = new List<Account>();
        
        for(Opportunity opp: oppList){
            for(OpportunityLineItem oli2: opp.OpportunityLineItems){
                
                System.debug('OppLineItems:::'+oli2.Product2.Name);
                
                if(oli2.Product2.Name.contains('Carefree Maintenance') && opp.Carefree_Maintenance_and_Support_Opt_in__c == False){
                    system.debug('*******');
                    opp.Carefree_Maintenance_and_Support_Opt_in__c = True;
                    UpdateOpp.add(opp);
                    
                    if(opp.Account.Carefree_Maintenance_and_Support_Opt_in__c == False){
                        opp.Account.Carefree_Maintenance_and_Support_Opt_in__c = true;
                        UpdateAcc.add(opp.Account);
                    }
                }
                
                if(oli2.Product2.Name.contains('Holiday Protection') && opp.Monthly_Holiday_Protection_Plan_Opt_in__c == False){
                    system.debug('*******');
                    opp.Monthly_Holiday_Protection_Plan_Opt_in__c = True;
                    UpdateOpp.add(opp);
                    
                    if(opp.Account.Monthly_Holiday_Protection_Plan_Opt_in__c == False){
                        opp.Account.Monthly_Holiday_Protection_Plan_Opt_in__c = true;
                        UpdateAcc.add(opp.Account);
                    }
                }
            }
        }
        
        Map<id,Account> acmap = new Map<id,Account>();
        acmap.putAll(UpdateAcc);
        
        Map<id,Opportunity> opmap = new Map<id,Opportunity>();
        opmap.putAll(UpdateOpp);
        
        if(opmap.size() > 0){
            Update opmap.values();
            system.debug(opmap.size()+'Opportunity Updated sucessfully:'+opmap.values());
        }
        if(acmap.size() > 0){
            Update acmap.values();
            system.debug(acmap.size()+'Account Updated sucessfully:'+acmap.values());
        }
    }
    
    public static void removeProductName(List<OpportunityLineItem> newList){
        system.debug('Called from OpportunityLineItem Trigger::::');
        
        Set<Id> allOppIds = new Set<Id>();
        
        //Get the OpportunityID related to the OpportunityLineItems
        system.debug('****'+newList);
        for(OpportunityLineItem oli : newList) {
            allOppIds.add(oli.OpportunityId);
            system.debug('****'+allOppIds);
        }         
        
        //Retrieve all the OpportunityLineItems records related to the Opportunity
        List<Opportunity> oppList = [SELECT Id, Name, AccountId,
                                     Carefree_Maintenance_and_Support_Opt_in__c, Monthly_Holiday_Protection_Plan_Opt_in__c, 
                                     (SELECT Id, Product2.name, Product2.Storage_Days__c
                                      FROM OpportunityLineItems)
                                     FROM Opportunity WHERE Id=:allOppIds]; 
        
        
        List<Opportunity> UpdateOpp2 = new List<Opportunity>();
        List<Account> UpdateAcct = new List<Account>();
        
        //Loop through all records to find out the product name contains 'Carefree Maintenance' or 'Holiday Protection'
        for(Opportunity o: oppList){
            for(OpportunityLineItem li: o.OpportunityLineItems){
                system.debug('*******'+li.Product2.Name);
                
                if(li.Product2.Name == Null){
                    System.debug('Opportunity Line Items are Null');
                }
                
                else if(!li.Product2.Name.contains('Carefree Maintenance') && o.Carefree_Maintenance_and_Support_Opt_in__c == True){
                    system.debug('****');
                    o.Carefree_Maintenance_and_Support_Opt_in__c = False;
                    UpdateOpp2.add(o);  
                }
                else if(!li.Product2.Name.contains('Holiday Protection') && o.Monthly_Holiday_Protection_Plan_Opt_in__c == True){
                    system.debug('****');
                    o.Monthly_Holiday_Protection_Plan_Opt_in__c = False;
                    UpdateOpp2.add(o);
                }
                else{
                    system.debug('Condition not matched::::');
                }
                break;
            }
        }
        
        Set<Id> accIds = new Set<Id>();
        for(Opportunity opt : oppList) {
            accIds.add(opt.AccountId);
            system.debug('****'+accIds);
        }  
        
        List<Account> accList = [SELECT Id, Name, Carefree_Maintenance_and_Support_Opt_in__c, Monthly_Holiday_Protection_Plan_Opt_in__c, 
                                 (SELECT Id, Name, Carefree_Maintenance_and_Support_Opt_in__c, Monthly_Holiday_Protection_Plan_Opt_in__c
                                  FROM Opportunities where Carefree_Maintenance_and_Support_Opt_in__c = True OR Monthly_Holiday_Protection_Plan_Opt_in__c = True) 
                                 FROM Account WHERE Id=:accIds];
        
        
        for(Account ac: accList){
            for(Opportunity opty: ac.Opportunities){
                
                system.debug('ac.Opportunities.size()'+ac.Opportunities.size());
                
                if(ac.Opportunities.size() >1){
                    break;
                }
                else{
                    if(ac.Opportunities.size() >= 0 || ac.Opportunities.size() <=1){
                        
                        system.debug('Opportunity List:'+Opty);
                        if(ac.Carefree_Maintenance_and_Support_Opt_in__c ==True && Opty.Carefree_Maintenance_and_Support_Opt_in__c == True){
                            ac.Carefree_Maintenance_and_Support_Opt_in__c = False;
                            UpdateAcct.add(ac);
                        }
                        
                        else if(ac.Monthly_Holiday_Protection_Plan_Opt_in__c ==True && Opty.Monthly_Holiday_Protection_Plan_Opt_in__c == True){
                            ac.Monthly_Holiday_Protection_Plan_Opt_in__c = False;
                            UpdateAcct.add(ac);
                        }
                        else{
                            system.debug('*****');
                        }
                    }
                }
            }
        }
        
        Map<id,Account> acmap = new Map<id,Account>();
        acmap.putAll(UpdateAcct);
        
        Map<id,Opportunity> opmap2 = new Map<id,Opportunity>();
        opmap2.putAll(UpdateOpp2);
        
        if(opmap2.size() > 0){
            Update opmap2.values();
            system.debug(opmap2.size()+'Opportunity Updated sucessfully:'+opmap2.values());
        }
        
        if(acmap.size() > 0){
            Update acmap.values();
            system.debug(acmap.size()+'Account Updated sucessfully:'+acmap.values());
        }
    }
}