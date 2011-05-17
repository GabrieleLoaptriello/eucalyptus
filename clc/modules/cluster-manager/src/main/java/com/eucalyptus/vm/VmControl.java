/*******************************************************************************
 *Copyright (c) 2009  Eucalyptus Systems, Inc.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, only version 3 of the License.
 * 
 * 
 *  This file is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 * 
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *  Please contact Eucalyptus Systems, Inc., 130 Castilian
 *  Dr., Goleta, CA 93101 USA or visit <http://www.eucalyptus.com/licenses/>
 *  if you need additional information or have any questions.
 * 
 *  This file may incorporate work covered under the following copyright and
 *  permission notice:
 * 
 *    Software License Agreement (BSD License)
 * 
 *    Copyright (c) 2008, Regents of the University of California
 *    All rights reserved.
 * 
 *    Redistribution and use of this software in source and binary forms, with
 *    or without modification, are permitted provided that the following
 *    conditions are met:
 * 
 *      Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *      Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 * 
 *    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 *    IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *    TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *    PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 *    OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *    EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *    PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *    PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *    LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *    NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. USERS OF
 *    THIS SOFTWARE ACKNOWLEDGE THE POSSIBLE PRESENCE OF OTHER OPEN SOURCE
 *    LICENSED MATERIAL, COPYRIGHTED MATERIAL OR PATENTED MATERIAL IN THIS
 *    SOFTWARE, AND IF ANY SUCH MATERIAL IS DISCOVERED THE PARTY DISCOVERING
 *    IT MAY INFORM DR. RICH WOLSKI AT THE UNIVERSITY OF CALIFORNIA, SANTA
 *    BARBARA WHO WILL THEN ASCERTAIN THE MOST APPROPRIATE REMEDY, WHICH IN
 *    THE REGENTS' DISCRETION MAY INCLUDE, WITHOUT LIMITATION, REPLACEMENT
 *    OF THE CODE SO IDENTIFIED, LICENSING OF THE CODE SO IDENTIFIED, OR
 *    WITHDRAWAL OF THE CODE CAPABILITY TO THE EXTENT NEEDED TO COMPLY WITH
 *    ANY SUCH LICENSES OR RIGHTS.
 *******************************************************************************/
/*
 *
 * Author: chris grzegorczyk <grze@eucalyptus.com>
 */

package com.eucalyptus.vm;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.mule.RequestContext;
import com.eucalyptus.auth.Accounts;
import com.eucalyptus.auth.AuthException;
import com.eucalyptus.auth.Permissions;
import com.eucalyptus.auth.policy.PolicySpec;
import com.eucalyptus.auth.principal.Account;
import com.eucalyptus.auth.principal.User;
import com.eucalyptus.cluster.Cluster;
import com.eucalyptus.cluster.Clusters;
import com.eucalyptus.cluster.VmInstance;
import com.eucalyptus.cluster.VmInstances;
import com.eucalyptus.cluster.callback.BundleCallback;
import com.eucalyptus.cluster.callback.CancelBundleCallback;
import com.eucalyptus.cluster.callback.ConsoleOutputCallback;
import com.eucalyptus.cluster.callback.PasswordDataCallback;
import com.eucalyptus.cluster.callback.RebootCallback;
import com.eucalyptus.context.Context;
import com.eucalyptus.context.Contexts;
import com.eucalyptus.context.ServiceContext;
import com.eucalyptus.records.EventType;
import com.eucalyptus.util.EucalyptusCloudException;
import com.eucalyptus.util.FullName;
import com.eucalyptus.util.Lookups;
import com.eucalyptus.util.async.Callbacks;
import com.eucalyptus.vm.SystemState.Reason;
import edu.ucsb.eucalyptus.cloud.VmAllocationInfo;
import edu.ucsb.eucalyptus.cloud.entities.SystemConfiguration;
import edu.ucsb.eucalyptus.msgs.BundleInstanceResponseType;
import edu.ucsb.eucalyptus.msgs.BundleInstanceType;
import edu.ucsb.eucalyptus.msgs.BundleTask;
import edu.ucsb.eucalyptus.msgs.CancelBundleTaskResponseType;
import edu.ucsb.eucalyptus.msgs.CancelBundleTaskType;
import edu.ucsb.eucalyptus.msgs.DescribeBundleTasksResponseType;
import edu.ucsb.eucalyptus.msgs.DescribeBundleTasksType;
import edu.ucsb.eucalyptus.msgs.DescribeInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.DescribeInstancesType;
import edu.ucsb.eucalyptus.msgs.EucalyptusErrorMessageType;
import edu.ucsb.eucalyptus.msgs.GetConsoleOutputResponseType;
import edu.ucsb.eucalyptus.msgs.TerminateInstancesItemType;
import com.eucalyptus.records.EventRecord;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import edu.ucsb.eucalyptus.cloud.VmAllocationInfo;
import edu.ucsb.eucalyptus.msgs.BaseMessage;
import edu.ucsb.eucalyptus.msgs.CreatePlacementGroupResponseType;
import edu.ucsb.eucalyptus.msgs.CreatePlacementGroupType;
import edu.ucsb.eucalyptus.msgs.CreateTagsResponseType;
import edu.ucsb.eucalyptus.msgs.CreateTagsType;
import edu.ucsb.eucalyptus.msgs.DeletePlacementGroupResponseType;
import edu.ucsb.eucalyptus.msgs.DeletePlacementGroupType;
import edu.ucsb.eucalyptus.msgs.DeleteTagsResponseType;
import edu.ucsb.eucalyptus.msgs.DeleteTagsType;
import edu.ucsb.eucalyptus.msgs.DescribeBundleTasksResponseType;
import edu.ucsb.eucalyptus.msgs.DescribeBundleTasksType;
import edu.ucsb.eucalyptus.msgs.DescribeInstanceAttributeResponseType;
import edu.ucsb.eucalyptus.msgs.DescribeInstanceAttributeType;
import edu.ucsb.eucalyptus.msgs.DescribeInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.DescribeInstancesType;
import edu.ucsb.eucalyptus.msgs.DescribePlacementGroupsResponseType;
import edu.ucsb.eucalyptus.msgs.DescribePlacementGroupsType;
import edu.ucsb.eucalyptus.msgs.DescribeTagsResponseType;
import edu.ucsb.eucalyptus.msgs.DescribeTagsType;
import edu.ucsb.eucalyptus.msgs.GetConsoleOutputResponseType;
import edu.ucsb.eucalyptus.msgs.GetConsoleOutputType;
import edu.ucsb.eucalyptus.msgs.GetPasswordDataResponseType;
import edu.ucsb.eucalyptus.msgs.GetPasswordDataType;
import edu.ucsb.eucalyptus.msgs.ModifyInstanceAttributeResponseType;
import edu.ucsb.eucalyptus.msgs.ModifyInstanceAttributeType;
import edu.ucsb.eucalyptus.msgs.MonitorInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.MonitorInstancesType;
import edu.ucsb.eucalyptus.msgs.RebootInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.RebootInstancesType;
import edu.ucsb.eucalyptus.msgs.ResetInstanceAttributeResponseType;
import edu.ucsb.eucalyptus.msgs.ResetInstanceAttributeType;
import edu.ucsb.eucalyptus.msgs.StartInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.StartInstancesType;
import edu.ucsb.eucalyptus.msgs.StopInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.StopInstancesType;
import edu.ucsb.eucalyptus.msgs.TerminateInstancesItemType;
import edu.ucsb.eucalyptus.msgs.TerminateInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.TerminateInstancesType;
import edu.ucsb.eucalyptus.msgs.UnmonitorInstancesResponseType;
import edu.ucsb.eucalyptus.msgs.UnmonitorInstancesType;

public class VmControl {
  
  private static Logger LOG = Logger.getLogger( VmControl.class );
  

  public VmAllocationInfo allocate( VmAllocationInfo vmAllocInfo ) throws EucalyptusCloudException {
    return vmAllocInfo;
  }
  
  public DescribeInstancesResponseType describeInstances( DescribeInstancesType msg ) throws EucalyptusCloudException {
    DescribeInstancesResponseType reply = ( DescribeInstancesResponseType ) msg.getReply( );
    try {
      reply.setReservationSet( SystemState.handle( msg ) );
    } catch ( Exception e ) {
      LOG.error( e );
      LOG.debug( e, e );
      throw new EucalyptusCloudException( e.getMessage( ) );
    }
    return reply;
  }
  
  public TerminateInstancesResponseType terminateInstances( final TerminateInstancesType request ) throws EucalyptusCloudException {
    TerminateInstancesResponseType reply = request.getReply( );
    try {
      Context ctx = Contexts.lookup( );
      final List<TerminateInstancesItemType> results = reply.getInstancesSet( );
      Iterables.all( request.getInstancesSet( ), new Predicate<String>( ) {
        @Override
        public boolean apply( String instanceId ) {
          try {
            VmInstance v = VmInstances.getInstance( ).lookup( instanceId );
            if ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, instanceId, v.getOwner( ) ) ) {
              int oldCode = v.getState( ).getCode( ), newCode = VmState.SHUTTING_DOWN.getCode( );
              String oldState = v.getState( ).getName( ), newState = VmState.SHUTTING_DOWN.getName( );
              results.add( new TerminateInstancesItemType( v.getInstanceId( ), oldCode, oldState, newCode, newState ) );
              if ( VmState.RUNNING.equals( v.getState( ) ) || VmState.PENDING.equals( v.getState( ) ) ) {
                v.setState( VmState.SHUTTING_DOWN, Reason.USER_TERMINATED );
              }
            }
            return true;
          } catch ( NoSuchElementException e ) {
            try {
              VmInstances.getInstance( ).lookupDisabled( instanceId ).setState( VmState.BURIED, Reason.BURIED );
              return true;
            } catch ( NoSuchElementException e1 ) {
              return false;
            }
          }
        }
      } );
      reply.set_return( !reply.getInstancesSet( ).isEmpty( ) );
      return reply;
    } catch ( Throwable e ) {
      LOG.error( e );
      LOG.debug( e, e );
      throw new EucalyptusCloudException( e.getMessage( ) );
    }
  }
  
  public RebootInstancesResponseType rebootInstances( final RebootInstancesType request ) throws EucalyptusCloudException {
    RebootInstancesResponseType reply = ( RebootInstancesResponseType ) request.getReply( );
    try {
      Context ctx = Contexts.lookup( );
      boolean result = Iterables.any( request.getInstancesSet( ), new Predicate<String>( ) {
        @Override
        public boolean apply( String instanceId ) {
          try {
            VmInstance v = VmInstances.getInstance( ).lookup( instanceId );
            if ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, instanceId, v.getOwner( ) ) ) {
              Callbacks.newRequest( new RebootCallback( v.getInstanceId( ) ).regarding( request ) ).dispatch( v.getClusterName( ) );
              return true;
            } else {
              return false;
            }
          } catch ( NoSuchElementException e ) {
            return false;
          }
        }
      } );
      reply.set_return( result );
      return reply;
    } catch ( Exception e ) {
      LOG.error( e );
      LOG.debug( e, e );
      throw new EucalyptusCloudException( e.getMessage( ) );
    }
  }
  
  public void getConsoleOutput( GetConsoleOutputType request ) throws EucalyptusCloudException {
    VmInstance v = null;
    try {
      v = VmInstances.getInstance( ).lookup( request.getInstanceId( ) );
    } catch ( NoSuchElementException e2 ) {
      try {
        v = VmInstances.getInstance( ).lookupDisabled( request.getInstanceId( ) );
        GetConsoleOutputResponseType reply = request.getReply( );
        reply.setInstanceId( request.getInstanceId( ) );
        reply.setTimestamp( new Date( ) );
        reply.setOutput( v.getConsoleOutputString( ) );
        ServiceContext.response( reply );
      } catch ( NoSuchElementException ex ) {
        throw new EucalyptusCloudException( "No such instance: " + request.getInstanceId( ) );
      }
    }
    if ( !Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, request.getInstanceId( ), v.getOwner( ) ) ) {
      throw new EucalyptusCloudException( "Permission denied for vm: " + request.getInstanceId( ) );
    } else if ( !VmState.RUNNING.equals( v.getState( ) ) ) {
      GetConsoleOutputResponseType reply = request.getReply( );
      reply.setInstanceId( request.getInstanceId( ) );
      reply.setTimestamp( new Date( ) );
      reply.setOutput( v.getConsoleOutputString( ) );
      ServiceContext.response( reply );
    } else {
      Cluster cluster = null;
      try {
        cluster = Clusters.getInstance( ).lookup( v.getClusterName( ) );
      } catch ( NoSuchElementException e1 ) {
        throw new EucalyptusCloudException( "Failed to find cluster info for '" + v.getClusterName( ) + "' related to vm: " + request.getInstanceId( ) );
      }
      RequestContext.getEventContext( ).setStopFurtherProcessing( true );
      Callbacks.newRequest( new ConsoleOutputCallback( request ) ).dispatch( cluster.getConfiguration( ) );
    }
  }
  
  public DescribeBundleTasksResponseType describeBundleTasks( DescribeBundleTasksType request ) throws EucalyptusCloudException {
    Context ctx = Contexts.lookup( );
    DescribeBundleTasksResponseType reply = request.getReply( );
    if ( request.getBundleIds( ).isEmpty( ) ) {
      for ( VmInstance v : VmInstances.getInstance( ).listValues( ) ) {
        if ( v.isBundling( ) && ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, v.getInstanceId( ), v.getOwner( ) ) ) ) {
          reply.getBundleTasks( ).add( v.getBundleTask( ) );
        }
      }
      for ( VmInstance v : VmInstances.getInstance( ).listDisabledValues( ) ) {
        if ( v.isBundling( ) && ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, v.getInstanceId( ), v.getOwner( ) ) ) ) {
          reply.getBundleTasks( ).add( v.getBundleTask( ) );
        }
      }
    } else {
      for ( String bundleId : request.getBundleIds( ) ) {
        try {
          VmInstance v = VmInstances.getInstance( ).lookupByBundleId( bundleId );
          if ( v.isBundling( ) && ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, v.getInstanceId( ), v.getOwner( ) ) ) ) {
            reply.getBundleTasks( ).add( v.getBundleTask( ) );
          }
        } catch ( NoSuchElementException e ) {}
      }
    }
    return reply;
  }
  public UnmonitorInstancesResponseType unmonitorInstances(UnmonitorInstancesType request) {
    UnmonitorInstancesResponseType reply = request.getReply( );
    return reply;
  }
  public StartInstancesResponseType startInstances(StartInstancesType request) {
    StartInstancesResponseType reply = request.getReply( );
    return reply;
  }

  public StopInstancesResponseType stopInstances(StopInstancesType request) {
    StopInstancesResponseType reply = request.getReply( );
    return reply;
  }
  public ResetInstanceAttributeResponseType resetInstanceAttribute(ResetInstanceAttributeType request) {
    ResetInstanceAttributeResponseType reply = request.getReply( );
    return reply;
  }
  public MonitorInstancesResponseType monitorInstances(MonitorInstancesType request) {
    MonitorInstancesResponseType reply = request.getReply( );
    return reply;
  }
  public ModifyInstanceAttributeResponseType modifyInstanceAttribute(ModifyInstanceAttributeType request) {
    ModifyInstanceAttributeResponseType reply = request.getReply( );
    return reply;
  }
  public DescribeTagsResponseType describeTags(DescribeTagsType request) {
    DescribeTagsResponseType reply = request.getReply( );
    return reply;
  }

  public DescribePlacementGroupsResponseType describePlacementGroups(DescribePlacementGroupsType request) {
    DescribePlacementGroupsResponseType reply = request.getReply( );
    return reply;
  }

  public DescribeInstanceAttributeResponseType describeInstanceAttribute(DescribeInstanceAttributeType request) {
    DescribeInstanceAttributeResponseType reply = request.getReply( );
    return reply;
  }

  public DeleteTagsResponseType deleteTags(DeleteTagsType request) {
    DeleteTagsResponseType reply = request.getReply( );
    return reply;
  }

  public DeletePlacementGroupResponseType deletePlacementGroup(DeletePlacementGroupType request) {
    DeletePlacementGroupResponseType reply = request.getReply( );
    return reply;
  }

  public CreateTagsResponseType createTags(CreateTagsType request) {
    CreateTagsResponseType reply = request.getReply( );
    return reply;
  }

  public CreatePlacementGroupResponseType createPlacementGroup(CreatePlacementGroupType request) {
    CreatePlacementGroupResponseType reply = request.getReply( );
    return reply;
  }

  public CancelBundleTaskResponseType cancelBundleTask( CancelBundleTaskType request ) throws EucalyptusCloudException {
    CancelBundleTaskResponseType reply = request.getReply( );
    reply.set_return( true );
    Context ctx = Contexts.lookup( );
    try {
      VmInstance v = VmInstances.getInstance( ).lookupByBundleId( request.getBundleId( ) );
      if ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, v.getInstanceId( ), v.getOwner( ) ) ) {
        v.getBundleTask( ).setState( "canceling" );
        LOG.info( EventRecord.here( BundleCallback.class, EventType.BUNDLE_CANCELING, ctx.getUserFullName( ).toString( ), v.getBundleTask( ).getBundleId( ),
                                    v.getInstanceId( ) ) );
        
        Cluster cluster = Clusters.getInstance( ).lookup( v.getClusterName( ) );
        
        request.setInstanceId( v.getInstanceId( ) );
        reply.setTask( v.getBundleTask( ) );
        Callbacks.newClusterRequest( new CancelBundleCallback( request ) ).dispatch( cluster.getConfiguration( ) );
        return reply;
      } else {
        throw new EucalyptusCloudException( "Failed to find bundle task: " + request.getBundleId( ) );
      }
    } catch ( NoSuchElementException e ) {
      throw new EucalyptusCloudException( "Failed to find bundle task: " + request.getBundleId( ) );
    }
  }
  
  public BundleInstanceResponseType bundleInstance( BundleInstanceType request ) throws EucalyptusCloudException {
    BundleInstanceResponseType reply = request.getReply( );//TODO: check if the instance has platform windows.
    reply.set_return( true );
    String walrusUrl = SystemConfiguration.getWalrusUrl( );
    String instanceId = request.getInstanceId( );
    Context ctx = Contexts.lookup( );
    User user = ctx.getUser( );

    try {
      VmInstance v = VmInstances.getInstance( ).lookup( instanceId );
      if ( v.isBundling( ) ) {
        reply.setTask( v.getBundleTask( ) );
        return reply;
      } else if ( !"windows".equals( v.getPlatform( ) ) ) {
        throw new EucalyptusCloudException( "Failed to bundle requested vm because the platform is not 'windows': " + request.getInstanceId( ) );
      } else if ( !VmState.RUNNING.equals( v.getState( ) ) ) {
        throw new EucalyptusCloudException( "Failed to bundle requested vm because it is not currently 'running': " + request.getInstanceId( ) );
      } else if ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, instanceId, v.getOwner( ) ) ) {
        BundleTask bundleTask = new BundleTask( v.getInstanceId( ).replaceFirst( "i-", "bun-" ), v.getInstanceId( ), request.getBucket( ), request.getPrefix( ) );
        if ( v.startBundleTask( bundleTask ) ) {
          reply.setTask( bundleTask );
        } else if ( v.getBundleTask( ) == null ) {
          v.resetBundleTask( );
          v.startBundleTask( bundleTask );
          reply.setTask( bundleTask );
        } else {
          throw new EucalyptusCloudException( "Instance is already being bundled: " + v.getBundleTask( ).getBundleId( ) );
        }
        LOG
           .info( EventRecord
                             .here( BundleCallback.class, EventType.BUNDLE_PENDING, ctx.getUserFullName( ).toString( ), v.getBundleTask( ).getBundleId( ), v.getInstanceId( ) ) );
        BundleCallback callback = new BundleCallback( request );
        request.setUrl( walrusUrl );
        request.setAwsAccessKeyId( Accounts.getFirstActiveAccessKeyId( user ) );
        Callbacks.newClusterRequest( callback ).dispatch( v.getClusterName( ) );
        return reply;
      } else {
        throw new EucalyptusCloudException( "Failed to find instance: " + request.getInstanceId( ) );
      }
    } catch ( Exception e ) {
      throw new EucalyptusCloudException( "Failed to find instance: " + request.getInstanceId( ) );
    }
  }
  
  public void getPasswordData( GetPasswordDataType request ) throws Exception {
    try {
      Context ctx = Contexts.lookup( );
      Cluster cluster = null;
      VmInstance v = VmInstances.getInstance( ).lookup( request.getInstanceId( ) );
      if ( !VmState.RUNNING.equals( v.getState( ) ) ) {
        throw new NoSuchElementException( "Instance " + request.getInstanceId( ) + " is not in a running state." );
      }
      if ( Lookups.checkPrivilege( request, PolicySpec.VENDOR_EC2, PolicySpec.EC2_RESOURCE_INSTANCE, request.getInstanceId( ), v.getOwner( ) ) ) {
        cluster = Clusters.getInstance( ).lookup( v.getClusterName( ) );
      } else {
        throw new NoSuchElementException( "Instance " + request.getInstanceId( ) + " does not exist." );
      }
      RequestContext.getEventContext( ).setStopFurtherProcessing( true );
      if ( v.getPasswordData( ) == null ) {
        Callbacks.newClusterRequest( new PasswordDataCallback( request ) ).dispatch( cluster.getConfiguration( ) );
      } else {
        GetPasswordDataResponseType reply = request.getReply( );
        reply.set_return( true );
        reply.setOutput( v.getPasswordData( ) );
        reply.setTimestamp( new Date( ) );
        reply.setInstanceId( v.getInstanceId( ) );
        ServiceContext.dispatch( "ReplyQueue", reply );
      }
    } catch ( NoSuchElementException e ) {
      ServiceContext.dispatch( "ReplyQueue", new EucalyptusErrorMessageType( RequestContext.getEventContext( ).getService( ).getComponent( ).getClass( )
                                                                                           .getSimpleName( ), request, e.getMessage( ) ) );
    }
  }
  
}