/**********************************************************************
* Name:     OpportunityTrigger
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
* 1.0       29-Feb-2016     rwd           Initial Development           
*   
***********************************************************************/


trigger OpportunityTrigger on Opportunity (before insert, before update, after insert, after update) {

    System.debug('---> start OpportunityTrigger');
    if ( SG_ApexActivator.isDisabled('Disable_Opportunity_Triggers__c'))
    {
        System.debug('---> OpportunityTrigger; this trigger has been disabled via Custom Setting');
        return;
    }

    if ( Trigger.isAfter )
    {
        if ( Trigger.isInsert || Trigger.isUpdate )
        {
            
            System.debug('---> opp trigger; isAlreadyModified: ' + RecursiveTriggerHelper.isAlreadyModified());
            if ( !RecursiveTriggerHelper.isAlreadyModified() )
            {
                RecursiveTriggerHelper.setAlreadyModified();
                OpportunityHelper.createInstallCases( Trigger.new, Trigger.oldMap );
                
                
            }  
               List<Opportunity> oplist = new list<opportunity>();
               for(opportunity op :trigger.new)
               {
                 if( op.StageName == '60- Project Closed' && Trigger.oldmap.get(op.id).StageName!= op.StageName)
                 oplist.add(op);
                 
               }
               if(!oplist.isempty())
               OpportunityHelper.sendNotificationEmail(oplist);
               
        }
        if ( Trigger.isInsert && Trigger.isAfter)
        {
            OpportunityHelper.defaultProductOffering(trigger.new);
        }
        
    }
    
    if ( Trigger.isBefore && !Trigger.isDelete )
    {
        OpportunityValidator.validate(Trigger.new);
    }
    
    if ( Trigger.isAfter && Trigger.isUpdate )
    {
        OpportunityUpdater.updateOpp(Trigger.new);
        DamageWaiverHelper.calculate(Trigger.newMap, Trigger.oldMap );
    }

}