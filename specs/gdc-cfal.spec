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

BuildRequires:  systemd
Requires(post): systemd
Requires(preun): systemd
Requires(postun): systemd

Requires:       java >= 1.8.0
%description restapi
GoodData CFAL REST API

%prep
# looks like useless, but doesn't work without it
%setup -q -n %{name} -c

%build
mvn -DskipTests=true -Dmaven.test.skip=true --update-snapshots clean package

%install
rm -rf $RPM_BUILD_ROOT

mkdir -p $RPM_BUILD_ROOT%{_javadir}/cfal-restapi
mkdir -p $RPM_BUILD_ROOT%{_bindir}

cp cfal-restapi/target/cfal-restapi.jar $RPM_BUILD_ROOT%{_javadir}/cfal-restapi

install $(find -path '*/bin7/*' -executable -type f) $RPM_BUILD_ROOT%{_bindir}
install -d $RPM_BUILD_ROOT%{_unitdir}
install $(find -path '*/systemd/*') $RPM_BUILD_ROOT%{_unitdir}/

%post
%systemd_post

%preun
%systemd_preun cfal-restapi.service

%postun
%systemd_postun

%clean
rm -rf $RPM_BUILD_ROOT

%files restapi
%defattr(-,root,root,0755)
%{_bindir}/cfal-restapi
%{_javadir}/cfal-restapi/*
%{_unitdir}/cfal-restapi.service

%changelog

