<%@ page language="java" session="false"
	import="java.util.*,java.io.*,javax.xml.parsers.*,com.occamlab.te.index.SuiteEntry,com.occamlab.te.*,com.occamlab.te.web.*"%><%!
	Config Conf = null;
	List<String> organizationList = null;
	Map<String, List<String>> standardMap = null;
	Map<String, List<String>> versionMap = null;
	Map<String, List<String>> revisionMap = null;
//	Map<String, SuiteEntry> suiteMap = null;
//	Map<String, Suite> suiteMap = null;
	
	public void jspInit() {
		try {
			Conf = new Config();
			organizationList = Conf.getOrganizationList();
			standardMap = Conf.getStandardMap();
			versionMap = Conf.getVersionMap();
			revisionMap = Conf.getRevisionMap();
		} catch (Exception e) {	
			e.printStackTrace(System.out);
		}
	}%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!-- ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

  The contents of this file are subject to the Mozilla Public License
  Version 1.1 (the "License"); you may not use this file except in
  compliance with the License. You may obtain a copy of the License at
  http://www.mozilla.org/MPL/ 

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  the specific language governing rights and limitations under the License. 

  The Original Code is TEAM Engine.

  The Initial Developer of the Original Code is Northrop Grumman Corporation
  jointly with The National Technology Alliance.  Portions created by
  Northrop Grumman Corporation are Copyright (C) 2005-2006, Northrop
  Grumman Corporation. All Rights Reserved.

  Contributor(s): No additional contributors to date

 +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ -->
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-US" lang="en-US">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>Compliance Testing</title>
<script>

	function fillOrgAndSession(){ 
		 // this function is used to fill the category list on load
		<% 
		//Fill Organization
		for(int i = 0; i < organizationList.size(); i++){ 
		%>
		addOption(document.standardsForm.Organization, "<%=organizationList.get(i)%>", "<%=organizationList.get(i)%>", "");
		<%}//for loop
		
		//Fill Session
		File userdir = new File(Conf.getUsersDir(), request.getRemoteUser());
		//File userdir = new File(Conf.getUsersDir(), "tester1");
		String[] dirs = userdir.list();
		for (int i = 0; i < dirs.length; i++) {
			if (new File(new File(userdir, dirs[i]), "session.xml").exists()) {
				TestSession s = new TestSession();
				s.load(userdir, dirs[i]);
				String description = "Session "+s.getSessionId() + ":"+s.getDescription();
	    %>
		addOption(document.standardsForm.Session, "<%=s.getSessionId()%>", "<%=description%>", "");
		<%}//if exists
		}//for loop%>

	}

	function SelectStandard(){
		// ON selection of organization this function will work
		
		removeAllOptions(document.standardsForm.Standard);
		addOption(document.standardsForm.Standard, "", "Standard", "");
		<%
		for(int i=0;i<organizationList.size();i++){
		%>
			if(document.standardsForm.Organization.value == '<%=organizationList.get(i)%>'){
			<% 
				List<String> standardList = standardMap.get(organizationList.get(i)); 
				for(int j=0; j < standardList.size(); j++){
			%>
				addOption(document.standardsForm.Standard,"<%=standardList.get(j)%>", "<%=standardList.get(j)%>");
				<%}//loop j%>
			}//organization	
		<%}//loop i%>
	}//function

	
	function SelectVersion(){
		// ON selection of organization this function will work
		
		removeAllOptions(document.standardsForm.Version);
		addOption(document.standardsForm.Version, "", "Version", "");
		<%
		for(int i=0;i<organizationList.size();i++){
		%>
			if(document.standardsForm.Organization.value == '<%=organizationList.get(i)%>'){			
			<%	
				List<String> standardList = standardMap.get(organizationList.get(i)); 
				for(int j=0;j<standardList.size();j++){
			%>
				if(document.standardsForm.Standard.value == '<%=standardList.get(j)%>'){
				<% 
					List<String> versionList = versionMap.get(organizationList.get(i) + "_" + standardList.get(j)); 
					for(int k=0; k < versionList.size(); k++){
				%>
					addOption(document.standardsForm.Version,"<%=versionList.get(k)%>", "<%=versionList.get(k)%>");
					<%}//loop k%>
				}//standard
				<%}//loop j%>
			}//organization
		<%}//loop i%>
	}//function

	function SelectTest(){
		// ON selection of organization this function will work
		
		removeAllOptions(document.standardsForm.Test);
		addOption(document.standardsForm.Test, "", "Test", "");
		<%
		for(int i=0; i < organizationList.size(); i++){
		%>
			if(document.standardsForm.Organization.value == '<%=organizationList.get(i)%>'){			
			<%	
				List<String> standardList = standardMap.get(organizationList.get(i)); 
				for(int j=0; j < standardList.size(); j++){
				%>
					if(document.standardsForm.Standard.value == '<%=standardList.get(j)%>'){
					<% 
						List<String> versionList = versionMap.get(organizationList.get(i) + "_" + standardList.get(j)); 
					    for(int k=0; k < versionList.size(); k++){
					%>
						if(document.standardsForm.Version.value == '<%=versionList.get(k)%>'){
						<% 
							List<String> revisionList = revisionMap.get(organizationList.get(i) + "_" + standardList.get(j) + "_" + versionList.get(k)); 
						    for(int l=0; l < revisionList.size(); l++){
						%>
								addOption(document.standardsForm.Test,"<%=revisionList.get(l)%>", "<%=revisionList.get(l)%>");
							<%}//loop l%>
						}//version	
						<%}//loop k%>
					}//standard
				<%}//loop j%>
			}//organization
		<%}//loop i%>
	}//funciton

	
	function removeAllOptions(selectbox)
	{
		var i;
		for(i=selectbox.options.length-1;i>=0;i--)
		{
			//selectbox.options.remove(i);
			selectbox.remove(i);
		}
	}
	
	function addOption(selectbox, value, text )
	{
		var optn = document.createElement("OPTION");
		optn.text = text;
		optn.value = value;
	
		selectbox.options.add(optn);
	}
	
	
	function viewInformation(){
	    var sourceId = document.standardsForm.Standard.value + "-" + document.standardsForm.Version.value;
	    if(sourceId != "-"){
			alert("No Documentation Available for: "+sourceId)
<%--
			<%
			Set suiteLinks = suiteMap.keySet();         // The set of keys in the map.
		    Iterator suiteLinksIter = suiteLinks.iterator();
		    while (suiteLinksIter.hasNext()) {
		       Object sourceId = suiteLinksIter.next();  	 // Get the next key.
		       Suite suite = (Suite)suiteMap.get(sourceId);  // Get the value for that key.
			%>
				if(sourceId == '<%=sourceId%>'){
				<%	if(suite.getLink() != null){
					%>
						window.location = '<%=suite.getLink()%>'
					<%	
					}//not null
					else {
					%>
						alert("No Documentation Available for: "+sourceId)
					<%	
					}//null	
				%>
				}     
	    	<%}//loop iterator%>
--%>
	    }
	    else{
	    	alert("Plase select from available standards");
	    }
	}
	
	function viewSession(){
		window.location = 'viewSessionLog.jsp?session='+document.standardsForm.Session.value
	}
	function submitform() {
		var form = document.standardsForm;
	    var sourceId = form.Organization.value + "_" + form.Standard.value + "_" + form.Version.value + "_" + form.Test.value;
	    if(sourceId != "___"){
<%--
			<%
			Set suiteKeys = suiteMap.keySet();         // The set of keys in the map.
		    Iterator suiteKeyIter = suiteKeys.iterator();
		    while (suiteKeyIter.hasNext()) {
		       Object sourceId = suiteKeyIter.next();  	 // Get the next key.
		       Suite suite = (Suite)suiteMap.get(sourceId);  // Get the value for that key.
			%>
		    	if(sourceId == '<%=sourceId%>'){
					document.forms["standardsForm"].elements["suite"].value = '<%=suite.getKey()%>';
		    	}
--%>
				document.forms["standardsForm"].elements["sources"].value = sourceId;
	    	<%--}//loop iterator--%>
			document.standardsForm.submit();
	    }
	    else{
	    	alert("Please select from available standards");
	    }
	}
				
</script>
</head>
<body onload="fillOrgAndSession()" >
<%@ include file="header.jsp"%>
<h3>Available Standards: </h3>
<form name="standardsForm" action="test.jsp" method="post" >
<table border="1" width="60%" >
	<tr>
		<th width="15%">Organization</th>
		<th width="15%">Standard</th>
		<th width="15%">Version</th>
		<th width="15%">Test Suite Rev</th>
	</tr>
	<tr>
		<td>
			<select  id="Organization" name="Organization" onChange="SelectStandard();" >
			<option value="">Organization</option>
			</select>
		</td>
		<td>
			<select id="Standard" name="Standard" onChange="SelectVersion();" >
			<option value="">Standard</option>
			</select>
		</td>
		<td>
			<select id="Version" name="Version" onChange="SelectTest();" >
			<option value="">Version</option>
			</select>
		</td>
		<td>
			<select id="Test" name="Test">
			<option value="">Test</option>
			</select>
		</td>
	</tr>
</table >
<table border="0" width="30%" >
	<tr>
		<td>
			<input type="button" value="More Information" onclick="viewInformation()" />&nbsp;
		</td>
		<td>
			<input type="button" value="Start a new test session" onclick="submitform()" />&nbsp;
		</td>
	</tr>
</table >
<br />

<h3>Existing Test Sessions: </h3>
<table border="0" width="30%" >
	<tr>
		<td>
			<select  id="Session" name="Session" >
			<option value="">Session</option>
			</select>
		</td>
		<td>
			<input type="button" value="View session" onclick="viewSession()" />&nbsp;
		</td>
	</tr>
</table >

<input type="hidden" name="mode" value="test" />
<input type="hidden" id="description" name="description" value=""/>
<input type="hidden" id="sources" name="sources" />
<input type="hidden" id="suite" name="suite" />
<p></p>
</form>
<%@ include file="footer.jsp"%>
</body>
</html>