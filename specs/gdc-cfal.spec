%global base_name gdc-cfal
# Release tag updates
%define __jar_repack %{nil}

%global webapps %{_datadir}/tomcat/webapps

Name:           gdc-cfal
Version:        1.%{gdcversion}
Release:        1%{dist}
Summary:        Customer Facing Audit Log

Group:          Applications/Productivity
License:        Proprietary
URL:            https://github.com/gooddata/gdc-cfal
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
Source0:        %{name}.tar.gz

%description
%{summary}

%package restapi
Summary:        GoodData CFAL REST API
Group:          Applications/Productivity
AutoReqProv:    no
BuildRequires:  java-1.8.0-openjdk-devel
BuildRequires:  maven
Requires:       java >= 1.8.0
Requires:       tomcat
%description restapi
GoodData CFAL REST API

%prep
# looks like useless, but doesn't work without it
%setup -q -n %{name} -c

%build
mvn -DskipTests=true -Dmaven.test.skip=true --update-snapshots clean package

%install
rm -rf $RPM_BUILD_ROOT

# Web App
install -d $RPM_BUILD_ROOT%{webapps}
cp -a cfal-restapi/target/cfal-restapi $RPM_BUILD_ROOT%{webapps}

%clean
rm -rf $RPM_BUILD_ROOT

%files restapi
%defattr(-,root,root,0755)
%{webapps}/cfal-restapi/*

%changelog

